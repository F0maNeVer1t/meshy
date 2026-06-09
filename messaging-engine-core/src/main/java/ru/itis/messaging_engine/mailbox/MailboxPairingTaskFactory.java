package ru.itis.messaging_engine.mailbox;


import ru.itis.messaging_engine.api.mailbox.MailboxPairingTask;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface MailboxPairingTaskFactory {

	MailboxPairingTask createPairingTask(String qrCodePayload);

}
