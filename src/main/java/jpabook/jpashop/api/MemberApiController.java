package jpabook.jpashop.api;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreatememberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreatememberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreatememberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest requset){
        Member member = new Member();
        member.setName(requset.getName());
        Long id = memberService.join(member);
        return new CreatememberResponse(id);
    }

    @Data
    static class CreateMemberRequest{
        @NotEmpty
        private String name;

    }

    @Data
    static class CreatememberResponse{
        public CreatememberResponse(Long id) {
            this.id = id;
        }
        private Long id;
    }
}
