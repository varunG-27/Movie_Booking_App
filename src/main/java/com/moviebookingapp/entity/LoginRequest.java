package com.moviebookingapp.entity;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginRequest {
	@NotBlank
	private String loginId;
	@NotBlank
	private String password;

}
