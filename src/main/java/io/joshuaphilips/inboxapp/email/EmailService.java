package io.joshuaphilips.inboxapp.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import io.joshuaphilips.inboxapp.emaillist.EmailListItem;
import io.joshuaphilips.inboxapp.emaillist.EmailListItemKey;
import io.joshuaphilips.inboxapp.emaillist.EmailListRepository;
import io.joshuaphilips.inboxapp.folders.UnreadEmailStatsRepository;

@Service
public class EmailService {

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private EmailListRepository emailListRepository;

	@Autowired
	private UnreadEmailStatsRepository unreadEmailStatsRepository;

	public void sendEmail(String from, List<String> to, String subject, String body) {

		Email email = new Email();
		email.setTo(to);
		email.setFrom(from);
		email.setSubject(subject);
		email.setId(Uuids.timeBased());
		email.setBody(body);
		emailRepository.save(email);

		to.forEach(toId -> {
			EmailListItem item = createEmailListItem(to, subject, email, toId, "Inbox");
			emailListRepository.save(item);
			unreadEmailStatsRepository.incrementUnreadCount(toId, "Inbox");

		});

		EmailListItem senderItem = createEmailListItem(to, subject, email, from, "Sent Items");
		senderItem.setUnread(false);
		emailListRepository.save(senderItem);
	}

	private EmailListItem createEmailListItem(List<String> to, String subject, Email email, String itemOwner,
			String folder) {
		EmailListItemKey key = new EmailListItemKey();
		key.setId(itemOwner);
		key.setLabel(folder);
		key.setTimeUUID(email.getId());

		EmailListItem item = new EmailListItem();
		item.setKey(key);
		item.setSubject(subject);
		item.setTo(to);
		item.setUnread(true);
		return item;
	}
}
