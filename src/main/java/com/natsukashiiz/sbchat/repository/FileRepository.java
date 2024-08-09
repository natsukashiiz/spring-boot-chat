package com.natsukashiiz.sbchat.repository;

import com.natsukashiiz.sbchat.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByName(String name);

    Optional<File> findByUrl(String url);
}