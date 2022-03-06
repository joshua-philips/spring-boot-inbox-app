package io.joshuaphilips.inboxapp.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.joshuaphilips.inboxapp.email.Email;
import io.joshuaphilips.inboxapp.email.EmailRepository;
import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;

@Controller
public class EmailViewController {
	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private FolderService folderService;

	@Autowired
	private EmailRepository emailRepository;

	@GetMapping("/emails/{id}")
	public String emailView(@AuthenticationPrincipal OAuth2User principal, Model model, @PathVariable UUID id) {

		if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
			return "index";
		}

		String userId = principal.getAttribute("login");
		System.out.println(principal.getAttributes());

		// Fetch folders
		List<Folder> userFolders = folderRepository.findAllById(userId);
		model.addAttribute("userFolders", userFolders);

		List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
		model.addAttribute("defaultFolders", defaultFolders);

		Optional<Email> optionalEmail = emailRepository.findById(id);
		if (optionalEmail.isEmpty()) {
			return "inbox-page";
		}

		Email email = optionalEmail.get();
		String toIds = String.join(", ", email.getTo());
		model.addAttribute("email", email);
		model.addAttribute("toIds", toIds);
		return "email-page";

	}

}
