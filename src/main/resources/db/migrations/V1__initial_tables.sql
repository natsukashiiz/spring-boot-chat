CREATE TABLE `users`
(
    `id`           BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username`     VARCHAR(30)                         NOT NULL,
    `mobile`       VARCHAR(10)                         NOT NULL,
    `password`     VARCHAR(255)                        NOT NULL,
    `nickname`     VARCHAR(20) CHARACTER SET utf8mb4   NOT NULL,
    `avatar`       VARCHAR(255)                        NULL,
    `last_seen_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `version`      INT       DEFAULT 0                 NOT NULL,
    `created_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `deleted_at`   TIMESTAMP                           NULL,
    CONSTRAINT `users_username_uq` UNIQUE (`username`),
    CONSTRAINT `users_mobile_uq` UNIQUE (`mobile`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1000;

CREATE TABLE `friends`
(
    `id`         BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED                     NOT NULL,
    `friend_id`  BIGINT UNSIGNED                     NOT NULL,
    `status`     ENUM ('Apply', 'Friend', 'Blocked') NOT NULL,
    `version`    INT       DEFAULT 0                 NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP                           NULL,
    CONSTRAINT `friends_user_id_friend_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `friends_friend_id_user_id_fk` FOREIGN KEY (`friend_id`) REFERENCES `users` (`id`),
    CONSTRAINT `friends_user_id_friend_id_uq` UNIQUE (`user_id`, `friend_id`)
) ENGINE = InnoDB;

CREATE TABLE `rooms`
(
    `id`         BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `type`       ENUM ('Friend', 'Group')            NOT NULL,
    `owner_id`   BIGINT UNSIGNED                     NULL,
    `name`       VARCHAR(20) CHARACTER SET utf8mb4   NULL,
    `image`      VARCHAR(255)                        NULL,
    `version`    INT       DEFAULT 0                 NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP                           NULL,
    CONSTRAINT `rooms_owner_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB;

CREATE TABLE `room_members`
(
    `id`         BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `room_id`    BIGINT UNSIGNED                     NOT NULL,
    `user_id`    BIGINT UNSIGNED                     NOT NULL,
    `muted`      TINYINT   DEFAULT 0                 NOT NULL,
    `version`    INT       DEFAULT 0                 NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `deleted_at` TIMESTAMP                           NULL,
    CONSTRAINT `room_members_room_id_fk` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
    CONSTRAINT `room_members_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `room_members_room_id_user_id_uq` UNIQUE (`room_id`, `user_id`)
) ENGINE = InnoDB;

CREATE TABLE `messages`
(
    `id`         BIGINT UNSIGNED                                             NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `content`    VARCHAR(255) CHARACTER SET utf8mb4                          NOT NULL,
    `type`       ENUM ('Join', 'Leave', 'Block', 'Unblock', 'Text', 'Image') NOT NULL,
    `sender_id`  BIGINT UNSIGNED                                             NOT NULL,
    `room_id`    BIGINT UNSIGNED                                             NOT NULL,
    `version`    INT       DEFAULT 0                                         NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP                         NOT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP                         NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP                                                   NULL,
    CONSTRAINT `messages_sender_id_fk` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`),
    CONSTRAINT `messages_room_id_fk` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE = InnoDB;

CREATE TABLE `message_reads`
(
    `id`         BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `message_id` BIGINT UNSIGNED                     NOT NULL,
    `user_id`    BIGINT UNSIGNED                     NOT NULL,
    `version`    INT       DEFAULT 0                 NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` TIMESTAMP                           NULL,
    CONSTRAINT `message_reads_message_id_fk` FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`),
    CONSTRAINT `message_reads_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB;

CREATE TABLE `inboxes`
(
    `id`              BIGINT UNSIGNED                     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`         BIGINT UNSIGNED                     NOT NULL,
    `room_id`         BIGINT UNSIGNED                     NOT NULL,
    `last_message_id` BIGINT UNSIGNED                     NOT NULL,
    `unread_count`    INT       DEFAULT 0                 NOT NULL,
    `version`         INT       DEFAULT 0                 NOT NULL,
    `created_at`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `updated_at`      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`      TIMESTAMP                           NULL,
    CONSTRAINT `inboxes_user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `inboxes_room_id_fk` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
    CONSTRAINT `inboxes_user_id_room_id_uq` UNIQUE (`user_id`, `room_id`)
) ENGINE = InnoDB;