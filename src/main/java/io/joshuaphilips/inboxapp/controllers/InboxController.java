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

import io.joshuaphilips.inboxapp.email.Email;
import io.joshuaphilips.inboxapp.email.EmailRepository;
import io.joshuaphilips.inboxapp.emaillist.EmailListItem;
import io.joshuaphilips.inboxapp.emaillist.EmailListItemKey;
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
	private EmailRepository emailRepository;

	private static int count = 0;

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

			return "inbox-page";
		}

		return "index";
	}

	public void initializeData() {
		System.out.println("Saving to cassandra");
		folderRepository.save(new Folder("joshua-philips", "Inbox", "blue"));
		folderRepository.save(new Folder("joshua-philips", "Sent", "green"));
		folderRepository.save(new Folder("joshua-philips", "Important", "yellow"));

		for (int i = 0; i < 10; i++) {

			EmailListItemKey key = new EmailListItemKey();
			key.setId("joshua-philips");
			key.setLabel("Inbox");
			key.setTimeUUID(Uuids.timeBased());

			EmailListItem item = new EmailListItem();
			item.setKey(key);
			item.setTo(Arrays.asList("joshua-philips", "daniella"));
			item.setSubject("Subject " + i);
			item.setUnread(true);

			Email email = new Email();
			email.setId(key.getTimeUUID());
			email.setFrom("joshua-philips");
			email.setTo(item.getTo());
			email.setSubject(item.getSubject());
			email.setBody("Body " + i);

			emailListRepository.save(item);
			emailRepository.save(email);
		}
	}
}
