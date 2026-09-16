package com.gridline.dtoutage.service;

import com.gridline.dtoutage.domain.*;
import com.gridline.dtoutage.exception.DuplicateActiveOutageException;
import com.gridline.dtoutage.exception.InvalidRestorationException;
import com.gridline.dtoutage.exception.ResourceNotFoundException;
import com.gridline.dtoutage.repository.*;
import com.gridline.dtoutage.web.dto.ReportOutageRequest;
import com.gridline.dtoutage.web.dto.RestoreOutageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DtOutageService {

    private final DtOutageRepository outageRepository;
    private final DtMasterRepository dtMasterRepository;
    private final FaultCategoryRepository faultCategoryRepository;
    private final ChallengeCategoryRepository challengeCategoryRepository;
    private final OutageAuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public DtOutage reportOutage(ReportOutageRequest request) {
        DtMaster dt = dtMasterRepository.findById(request.dtId())
                .orElseThrow(() -> new ResourceNotFoundException("DT not found: " + request.dtId()));

        // UX-layer pre-check — the DB's partial unique index is the real,
        // atomic guarantee (see idx_one_active_outage_per_dt); this just
        // avoids a round trip to the DB for the common case.
        if (outageRepository.existsByDt_DtIdAndStatus(dt.getDtId(), OutageStatus.OUT)) {
            String existingRef = outageRepository
                    .findByStatusOrderByOutageDateDescOutageTimeDesc(OutageStatus.OUT).stream()
                    .filter(o -> o.getDt().getDtId().equals(dt.getDtId()))
                    .findFirst()
                    .map(DtOutage::getOutageRef)
                    .orElse("unknown");
            throw new DuplicateActiveOutageException(dt.getDtCode(), existingRef);
        }

        FaultCategory faultCategory = faultCategoryRepository.findById(request.faultCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Fault category not found"));
        ChallengeCategory challenge = request.challengeId() == null ? null :
                challengeCategoryRepository.findById(request.challengeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Challenge category not found"));

        User reporter = currentUserService.getOrCreateCurrentUser();

        DtOutage outage = DtOutage.builder()
                .outageRef(generateOutageRef())
                .dt(dt)
                .dtCodeSnapshot(dt.getDtCode())
                .businessUnitSnapshot(dt.getBusinessUnit())
                .undertakingSnapshot(dt.getUndertaking())
                .feederSnapshot(dt.getFeeder())
                .capacitySnapshot(dt.getCapacityKva())
                .bandSnapshot(dt.getSupplyBand())
                .outageDate(request.outageDate())
                .outageTime(request.outageTime())
                .faultCategory(faultCategory)
                .faultDescription(request.faultDescription())
                .challenge(challenge)
                .additionalComment(request.additionalComment())
                .status(OutageStatus.OUT)
                .reportedBy(reporter)
                .build();

        // Postgres will still reject this atomically via
        // idx_one_active_outage_per_dt if two requests race past the check
        // above — GlobalExceptionHandler translates that DataIntegrityViolationException.
        outage = outageRepository.save(outage);

        auditLogRepository.save(OutageAuditLog.builder()
                .outageId(outage.getOutageId())
                .action("REPORTED")
                .performedBy(reporter.getUserId())
                .build());

        return outage;
    }

    @Transactional
    public DtOutage restoreOutage(UUID outageId, RestoreOutageRequest request) {
        DtOutage outage = outageRepository.findById(outageId)
                .orElseThrow(() -> new ResourceNotFoundException("Outage not found: " + outageId));

        if (outage.getStatus() != OutageStatus.OUT) {
            throw new InvalidRestorationException("Outage " + outage.getOutageRef() + " is not currently active.");
        }

        LocalDateTime outageDateTime = LocalDateTime.of(outage.getOutageDate(), outage.getOutageTime());
        LocalDateTime restorationDateTime = LocalDateTime.of(request.restorationDate(), request.restorationTime());
        if (restorationDateTime.isBefore(outageDateTime)) {
            throw new InvalidRestorationException("Restoration time cannot be before the outage was reported.");
        }

        ChallengeCategory challenge = outage.getChallenge();
        if (request.challengeId() != null) {
            challenge = challengeCategoryRepository.findById(request.challengeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Challenge category not found"));
        }

        User restorer = currentUserService.getOrCreateCurrentUser();

        outage.setRestorationDate(request.restorationDate());
        outage.setRestorationTime(request.restorationTime());
        outage.setRestorationRemarks(request.restorationRemarks());
        outage.setChallenge(challenge);
        outage.setStatus(OutageStatus.RESTORED);
        outage.setRestoredBy(restorer);
        outage.setRestoredAt(java.time.Instant.now());

        // The DB CHECK constraint (chk_restoration_after_outage) is the
        // authoritative guard for this — the isBefore() check above is just
        // a faster failure with a friendlier message.
        outage = outageRepository.save(outage);

        auditLogRepository.save(OutageAuditLog.builder()
                .outageId(outage.getOutageId())
                .action("RESTORED")
                .performedBy(restorer.getUserId())
                .build());

        return outage;
    }

    public List<DtOutage> findActive() {
        return outageRepository.findByStatusOrderByOutageDateDescOutageTimeDesc(OutageStatus.OUT);
    }

    public List<DtOutage> findByStatus(OutageStatus status) {
        return outageRepository.findByStatusOrderByOutageDateDescOutageTimeDesc(status);
    }

    public List<DtOutage> findAll() {
        return outageRepository.findAll();
    }

    private String generateOutageRef() {
        int year = Year.now().getValue();
        Long sequence = jdbcTemplate.queryForObject("SELECT nextval('outage_ref_sequence')", Long.class);
        if (sequence == null) {
            throw new IllegalStateException("Unable to allocate an outage reference number.");
        }
        return "OUT-%d-%05d".formatted(year, sequence);
    }
}
