package io.joshuaphilips.inboxapp.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;

@Controller
public class ComposeController {
	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private FolderService folderService;

	@GetMapping(value = "/compose")
	public String getComposePage(@AuthenticationPrincipal OAuth2User principal, Model model,
			@RequestParam(required = false) String to) {
		if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
			return "index";
		}
		String userId = principal.getAttribute("login");

		// Fetch folders
		List<Folder> userFolders = folderRepository.findAllById(userId);
		model.addAttribute("userFolders", userFolders);

		List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
		model.addAttribute("defaultFolders", defaultFolders);

		if (StringUtils.hasText(to)) {
			String[] splitIds = to.split(",");
			List<String> uniqueToIds = Arrays.asList(splitIds).stream().map(id -> StringUtils.trimWhitespace(id))
					.filter(id -> StringUtils.hasText(id)).distinct().collect(Collectors.toList());

			model.addAttribute("toIds", String.join(", ", uniqueToIds));
		}

		return "compose-page";
	}
}
