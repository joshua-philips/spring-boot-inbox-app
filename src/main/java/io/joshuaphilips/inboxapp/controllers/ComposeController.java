package io.joshuaphilips.inboxapp.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.joshuaphilips.inboxapp.email.EmailService;
import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;

@Controller
public class ComposeController {
	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private FolderService folderService;

	@Autowired
	private EmailService emailService;

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

		model.addAttribute("stats", folderService.mapCountToLabels(userId));

		List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
		model.addAttribute("defaultFolders", defaultFolders);

		List<String> uniqueToIds = splitIds(to);
		model.addAttribute("toIds", String.join(", ", uniqueToIds));

		return "compose-page";
	}

	private List<String> splitIds(String to) {
		if (!StringUtils.hasText(to)) {
			return new ArrayList<String>();
		}
		String[] splitIds = to.split(",");
		List<String> uniqueToIds = Arrays.asList(splitIds).stream().map(id -> StringUtils.trimWhitespace(id))
				.filter(id -> StringUtils.hasText(id)).distinct().collect(Collectors.toList());
		return uniqueToIds;
	}

	@PostMapping(value = "/sendemail")
	public ModelAndView sendEmail(@AuthenticationPrincipal OAuth2User principal,
			@RequestBody MultiValueMap<String, String> formData) {

		if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
			return new ModelAndView("redirect:/");
		}

		String from = principal.getAttribute("login");
		List<String> toIds = splitIds(formData.getFirst("toIds"));
		String subject = formData.getFirst("subject");
		String body = formData.getFirst("body");

		emailService.sendEmail(from, toIds, subject, body);

		return new ModelAndView("redirect:/?folder=Sent Items");
	}

}
