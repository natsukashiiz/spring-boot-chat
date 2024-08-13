ALTER TABLE `friends` ADD COLUMN `room_id` BIGINT UNSIGNED NULL AFTER `friend_id`;
ALTER TABLE `friends` ADD CONSTRAINT `fk_friends_room_id` FOREIGN KEY (`room_id`) REFERENCES `rooms`(`id`);