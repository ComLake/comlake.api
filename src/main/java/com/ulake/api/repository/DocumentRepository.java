package com.ulake.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ulake.api.models.Document;


public interface DocumentRepository extends JpaRepository<Document, Long> {
	Document findByTitle(String title);
	
	Document findByTitleContaining(String title);

}
