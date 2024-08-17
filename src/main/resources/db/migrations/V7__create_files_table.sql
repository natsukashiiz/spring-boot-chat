CREATE TABLE `files`
(
    `id`           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `owner_id`     BIGINT UNSIGNED                                   NULL,
    `room_id`      BIGINT UNSIGNED                                   NULL,
    `message_id`   BIGINT UNSIGNED                                   NULL,
    `type`         ENUM ('Image', 'Audio', 'Video', 'File', 'Other') NOT NULL,
    `name`         VARCHAR(255)                                      NOT NULL,
    `path`         VARCHAR(255)                                      NOT NULL,
    `url`          VARCHAR(255)                                      NOT NULL,
    `format`       VARCHAR(255)                                      NOT NULL,
    `content_type` VARCHAR(255)                                      NOT NULL,
    `size`         BIGINT UNSIGNED                                   NOT NULL,
    `version`      INT       DEFAULT 0                               NOT NULL,
    `created_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`   TIMESTAMP                                         NULL,
    CONSTRAINT files_owner_id_fk FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT files_room_id_fk FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT files_message_id_fk FOREIGN KEY (message_id) REFERENCES messages (id),
    CONSTRAINT files_name_uq UNIQUE (name),
    CONSTRAINT files_path_uq UNIQUE (path),
    CONSTRAINT files_url_uq UNIQUE (url)
) ENGINE = InnoDB;