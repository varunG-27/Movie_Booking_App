package com.moviebookingapp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "roles")
public class Role {
	
	@Id
	private String _id;
	
	private Roles name;
	
	public Role() {
		
	}
	
	public Role(Roles name) {
		this.name = name;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public Roles getName() {
		return name;
	}

	public void setName(Roles name) {
		this.name = name;
	}
	
	

}
