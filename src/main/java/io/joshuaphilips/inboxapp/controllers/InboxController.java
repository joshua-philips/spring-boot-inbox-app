package io.joshuaphilips.inboxapp.controllers;

import java.util.Arrays;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.joshuaphilips.inboxapp.email.EmailService;
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
	private FolderService folderService;

	@Autowired
	private EmailService emailService;

	static int count = 0;

	@GetMapping("/")
	public String homePage(@AuthenticationPrincipal OAuth2User principal, Model model,
			@RequestParam(required = false) String folder) {
		if (count == 0) {
			initializeData();
			count++;
		}

		if (principal != null && StringUtils.hasText(principal.getAttribute("login"))) {

			String userId = principal.getAttribute("login");

			// Fetch folders
			List<Folder> userFolders = folderRepository.findAllById(userId);
			model.addAttribute("userFolders", userFolders);

			List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
			model.addAttribute("defaultFolders", defaultFolders);

			model.addAttribute("stats", folderService.mapCountToLabels(userId));

			// Fetch messages
			if (!StringUtils.hasText(folder)) {
				folder = "Inbox";
			}

			List<EmailListItem> emailList = emailListRepository.findAllByKey_IdAndKey_Label(userId, folder);

			PrettyTime prettyTime = new PrettyTime();
			emailList.stream().forEach(emailItem -> emailItem.setAgoTimeString(
					prettyTime.format(new Date(Uuids.unixTimestamp(emailItem.getKey().getTimeUUID())))));
			model.addAttribute("emailList", emailList);
			model.addAttribute("folderName", folder);
			model.addAttribute("username", principal.getAttribute("name"));

			return "inbox-page";
		}

		return "index";
	}

	public void initializeData() {
		System.out.println("Saving to cassandra");
		folderRepository.save(new Folder("joshua-philips", "Work", "blue"));
		folderRepository.save(new Folder("joshua-philips", "Friends", "green"));
		folderRepository.save(new Folder("joshua-philips", "Family", "yellow"));

		for (int i = 0; i < 10; i++) {
			emailService.sendEmail("joshua-philips", Arrays.asList("joshua-philips", "daniella"), "Subject " + i,
					"Body " + i);
		}
	}
}
