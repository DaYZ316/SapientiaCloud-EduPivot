package com.dayz.sapientiacloud_edupivot.live.schedule;

import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.constant.LiveRoomConstants;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveRoomAutoEndScheduler {

    private final ILiveRoomService liveRoomService;

    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void autoEndExpiredLives() {
        List<LiveRoom> livingRooms = liveRoomService.listRooms(LiveRoomConstants.STATUS_LIVING, null, null);
        for (LiveRoom livingRoom : livingRooms) {
            if (livingRoom == null || livingRoom.getId() == null) {
                continue;
            }
            try {
                if (liveRoomService.shouldAutoEnd(livingRoom.getId())) {
                    liveRoomService.endLive(livingRoom.getId());
                }
            } catch (BusinessException exception) {
                log.warn("skip auto end live room {}, reason: {}", livingRoom.getId(), exception.getMessage());
            } catch (Exception exception) {
                log.error("auto end live room {} failed", livingRoom.getId(), exception);
            }
        }
    }
}
