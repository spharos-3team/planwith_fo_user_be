package com.planwith.user.application.service;

import com.planwith.user.application.port.out.FollowPort;
import com.planwith.user.application.port.out.UserRepositoryPort;
import com.planwith.user.domain.user.LoginType;
import com.planwith.user.domain.user.User;
import com.planwith.user.domain.user.UserStatus;
import com.planwith.user.global.exception.CustomException;
import com.planwith.user.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private FollowPort followPort;
    @Mock private GradeService gradeService;
    @InjectMocks private FollowService followService;

    @Test
    @DisplayName("follow creates active relation")
    void follow_success() {
        User follower = sampleUser(1L, "uuid-1", "me");
        User followee = sampleUser(2L, "uuid-2", "you");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(follower));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(followee));
        given(followPort.find("uuid-1", "uuid-2")).willReturn(Optional.empty());

        followService.follow(1L, "uuid-2");

        ArgumentCaptor<FollowPort.FollowRelation> captor = ArgumentCaptor.forClass(FollowPort.FollowRelation.class);
        verify(followPort).save(captor.capture());
        assertThat(captor.getValue().followerMemberUuid()).isEqualTo("uuid-1");
        assertThat(captor.getValue().followeeMemberUuid()).isEqualTo("uuid-2");
        assertThat(captor.getValue().active()).isTrue();
    }

    @Test
    @DisplayName("follow rejects self follow")
    void follow_self() {
        User user = sampleUser(1L, "uuid-1", "me");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(user));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-1")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> followService.follow(1L, "uuid-1"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CANNOT_FOLLOW_SELF);
    }

    @Test
    @DisplayName("follow rejects when already following")
    void follow_already() {
        User follower = sampleUser(1L, "uuid-1", "me");
        User followee = sampleUser(2L, "uuid-2", "you");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(follower));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(followee));
        given(followPort.find("uuid-1", "uuid-2"))
                .willReturn(Optional.of(new FollowPort.FollowRelation(9L, "f", "uuid-1", "uuid-2", true)));

        assertThatThrownBy(() -> followService.follow(1L, "uuid-2"))
                .isInstanceOf(CustomException.class)
                .extracting(ex -> ((CustomException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_FOLLOWING);
    }

    @Test
    @DisplayName("follow reactivates inactive relation")
    void follow_reactivate() {
        User follower = sampleUser(1L, "uuid-1", "me");
        User followee = sampleUser(2L, "uuid-2", "you");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(follower));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(followee));
        given(followPort.find("uuid-1", "uuid-2"))
                .willReturn(Optional.of(new FollowPort.FollowRelation(9L, "f-uuid", "uuid-1", "uuid-2", false)));

        followService.follow(1L, "uuid-2");

        ArgumentCaptor<FollowPort.FollowRelation> captor = ArgumentCaptor.forClass(FollowPort.FollowRelation.class);
        verify(followPort).save(captor.capture());
        assertThat(captor.getValue().followId()).isEqualTo(9L);
        assertThat(captor.getValue().followUuid()).isEqualTo("f-uuid");
        assertThat(captor.getValue().active()).isTrue();
    }

    @Test
    @DisplayName("unfollow deactivates active relation")
    void unfollow_success() {
        User follower = sampleUser(1L, "uuid-1", "me");
        User followee = sampleUser(2L, "uuid-2", "you");
        given(userRepositoryPort.findActiveById(1L)).willReturn(Optional.of(follower));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(followee));
        given(followPort.find("uuid-1", "uuid-2"))
                .willReturn(Optional.of(new FollowPort.FollowRelation(9L, "f-uuid", "uuid-1", "uuid-2", true)));

        followService.unfollow(1L, "uuid-2");

        ArgumentCaptor<FollowPort.FollowRelation> captor = ArgumentCaptor.forClass(FollowPort.FollowRelation.class);
        verify(followPort).save(captor.capture());
        assertThat(captor.getValue().active()).isFalse();
    }

    @Test
    @DisplayName("listFollowers maps active members")
    void listFollowers_success() {
        User target = sampleUser(2L, "uuid-2", "you");
        User follower = sampleUser(1L, "uuid-1", "me");
        given(userRepositoryPort.findActiveByMemberUuid("uuid-2")).willReturn(Optional.of(target));
        given(followPort.findFollowerUuids("uuid-2")).willReturn(List.of("uuid-1"));
        given(userRepositoryPort.findActiveByMemberUuid("uuid-1")).willReturn(Optional.of(follower));
        given(followPort.countFollowers("uuid-1")).willReturn(0L);
        given(followPort.countFollowing("uuid-1")).willReturn(1L);

        assertThat(followService.listFollowers("uuid-2")).hasSize(1);
        assertThat(followService.listFollowers("uuid-2").get(0).getNickname()).isEqualTo("me");
    }

    private static User sampleUser(Long id, String uuid, String nickname) {
        return User.builder()
                .id(id)
                .memberUuid(uuid)
                .nickname(nickname)
                .email(id + "@b.com")
                .loginType(LoginType.LOCAL)
                .status(UserStatus.ACTIVE)
                .role("USER")
                .build();
    }
}
