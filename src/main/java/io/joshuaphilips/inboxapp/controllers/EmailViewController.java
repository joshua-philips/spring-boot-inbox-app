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
import org.springframework.web.bind.annotation.RequestParam;

import io.joshuaphilips.inboxapp.email.Email;
import io.joshuaphilips.inboxapp.email.EmailRepository;
import io.joshuaphilips.inboxapp.emaillist.EmailListItem;
import io.joshuaphilips.inboxapp.emaillist.EmailListItemKey;
import io.joshuaphilips.inboxapp.emaillist.EmailListRepository;
import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;
import io.joshuaphilips.inboxapp.folders.UnreadEmailStatsRepository;

@Controller
public class EmailViewController {
	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private FolderService folderService;

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private EmailListRepository emailListRepository;

	@Autowired
	private UnreadEmailStatsRepository unreadEmailStatsRepository;

	@GetMapping("/emails/{id}")
	public String emailView(@AuthenticationPrincipal OAuth2User principal, Model model, @PathVariable UUID id,
			@RequestParam String folder) {

		if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
			return "index";
		}

		String userId = principal.getAttribute("login");

		// Fetch folders
		List<Folder> userFolders = folderRepository.findAllById(userId);
		model.addAttribute("userFolders", userFolders);

		List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
		model.addAttribute("defaultFolders", defaultFolders);
		model.addAttribute("username", principal.getAttribute("name"));

		Optional<Email> optionalEmail = emailRepository.findById(id);
		if (optionalEmail.isEmpty()) {
			return "inbox-page";
		}

		Email email = optionalEmail.get();
		String toIds = String.join(", ", email.getTo());

		// Check if user is allowed to view
		if (!userId.equals(email.getFrom()) && !email.getTo().contains(userId)) {
			return "redirect:/";
		}

		model.addAttribute("email", email);
		model.addAttribute("toIds", toIds);

		EmailListItemKey key = new EmailListItemKey();
		key.setId(userId);
		key.setLabel(folder);
		key.setTimeUUID(email.getId());

		Optional<EmailListItem> optionalEmailListItem = emailListRepository.findById(key);
		if (optionalEmailListItem.isPresent()) {
			EmailListItem emailListItem = optionalEmailListItem.get();
			if (emailListItem.isUnread()) {
				emailListItem.setUnread(false);
				emailListRepository.save(emailListItem);

				unreadEmailStatsRepository.decrementUnreadCount(userId, folder);
			}
		}

		model.addAttribute("stats", folderService.mapCountToLabels(userId));

		return "email-page";

	}

}
