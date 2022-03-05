package io.joshuaphilips.inboxapp.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;

@Controller
public class InboxController {

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	FolderService folderService;

	@GetMapping("/")
	public String homePage(@AuthenticationPrincipal OAuth2User principal, Model model) {

		if (principal != null && StringUtils.hasText(principal.getAttribute("login"))) {

			String userId = principal.getAttribute("login");

			System.out.println(principal.getAttributes());

			List<Folder> userFolders = folderRepository.findAllById(userId);
			model.addAttribute("userFolders", userFolders);

			List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
			model.addAttribute("defaultFolders", defaultFolders);

			return "inbox-page";
		}

		return "index";
	}
}
