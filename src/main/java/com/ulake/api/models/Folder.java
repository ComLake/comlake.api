package com.ulake.api.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "CLake_folders")
public class Folder extends Auditable<String> implements IEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "creator_id", nullable = false)
	private User creator;

	private Long parentId;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "folder")
	private Set<File> files = new HashSet<>();

	private String cid;

	private String name;

//	private String mimeType;
//
//	private String source;
//	
//	private String topics;

	private Long size;

	public Folder() {

	}

	public Folder(User creator, String name) {
		this.creator = creator;
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

//	public String getSource() {
//		return source;
//	}
//
//	public void setSource(String source) {
//		this.source = source;
//	}
//	
//	public String getTopics() {
//		return topics;
//	}
//
//	public void setTopics(String topics) {
//		this.topics = topics;
//	}

}