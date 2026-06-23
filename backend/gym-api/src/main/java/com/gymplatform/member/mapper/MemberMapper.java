package com.gymplatform.member.mapper;

import com.gymplatform.member.domain.Member;
import com.gymplatform.member.domain.MemberDocument;
import com.gymplatform.member.dto.MemberDocumentResponse;
import com.gymplatform.member.dto.MemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "bmi", expression = "java(member.computeBmi())")
    MemberResponse toResponse(Member member);

    MemberDocumentResponse toResponse(MemberDocument document);
}
