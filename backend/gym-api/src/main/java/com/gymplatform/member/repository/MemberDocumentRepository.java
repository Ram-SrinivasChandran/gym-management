package com.gymplatform.member.repository;

import com.gymplatform.member.domain.MemberDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDocumentRepository extends JpaRepository<MemberDocument, UUID> {

    List<MemberDocument> findByMemberId(UUID memberId);
}
