package io.joshuaphilips.inboxapp.emaillist;

import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailListRepository extends CassandraRepository<EmailListItem, EmailListItemKey> {

//	 List<EmailListItem> finadAllById(EmailListItemKey id);
	List<EmailListItem> findAllByKey_IdAndKey_Label(String id, String label);

}
