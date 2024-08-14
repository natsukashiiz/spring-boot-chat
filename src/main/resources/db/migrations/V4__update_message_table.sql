ALTER TABLE `messages` MODIFY COLUMN `content` VARCHAR(1024) CHARACTER SET utf8mb4 NULL;
ALTER TABLE `messages` MODIFY COLUMN `type` ENUM ('Text', 'Image', 'Audio', 'Video', 'File') NULL;
ALTER TABLE `messages` ADD COLUMN `action` ENUM ('SendMessage', 'EditMessage', 'JoinChat', 'LeaveChat', 'BlockUser', 'UnblockUser', 'CreateGroupChat', 'RenameGroupChat', 'ChangeGroupChatPhoto', 'RemoveGroupMember', 'AddGroupMember') NULL AFTER `content`;
ALTER TABLE `messages` ADD COLUMN `mention_id` BIGINT UNSIGNED NULL AFTER `room_id`;
ALTER TABLE `messages` ADD CONSTRAINT `messages_mention_id_fk` FOREIGN KEY (`mention_id`) REFERENCES `users` (`id`);
ALTER TABLE `messages` ADD COLUMN `reply_to_id` BIGINT UNSIGNED NULL AFTER `mention_id`;
ALTER TABLE `messages` ADD CONSTRAINT `messages_reply_to_id_fk` FOREIGN KEY (`reply_to_id`) REFERENCES `messages` (`id`);
