package io.joshuaphilips.inboxapp.controllers;

import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.joshuaphilips.inboxapp.emaillist.EmailListItem;
import io.joshuaphilips.inboxapp.emaillist.EmailListRepository;
import io.joshuaphilips.inboxapp.folders.Folder;
import io.joshuaphilips.inboxapp.folders.FolderRepository;
import io.joshuaphilips.inboxapp.folders.FolderService;

@Controller
public class InboxController {

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private EmailListRepository emailListRepository;

	@Autowired
	FolderService folderService;

	@GetMapping("/")
	public String homePage(@AuthenticationPrincipal OAuth2User principal, Model model) {

		if (principal != null && StringUtils.hasText(principal.getAttribute("login"))) {

			String userId = principal.getAttribute("login");
			System.out.println(principal.getAttributes());

			// Fetch folders
			List<Folder> userFolders = folderRepository.findAllById(userId);
			model.addAttribute("userFolders", userFolders);

			List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
			model.addAttribute("defaultFolders", defaultFolders);

			// Fetch messages
			String folderLabel = "Inbox";
			List<EmailListItem> emailList = emailListRepository.findAllByKey_IdAndKey_Label(userId, folderLabel);

			PrettyTime prettyTime = new PrettyTime();
			emailList.stream().forEach(emailItem -> emailItem.setAgoTimeString(
					prettyTime.format(new Date(Uuids.unixTimestamp(emailItem.getKey().getTimeUUID())))));
			model.addAttribute("emailList", emailList);

			return "inbox-page";
		}

		return "index";
	}
}
