package ru.itis.messaging_engine.api.mailbox;

import ru.itis.messaging_engine.api.FormatException;
import ru.itis.messaging_engine.api.db.DbException;
import ru.itis.messaging_engine.api.db.Transaction;
import ru.itis.messaging_engine.api.lifecycle.IoExecutor;
import ru.itis.messaging_engine.api.mailbox.event.OwnMailboxConnectionStatusEvent;

import javax.annotation.Nullable;

public interface MailboxManager {

	/**
	 * @return true if a mailbox is already paired.
	 */
	boolean isPaired(Transaction txn) throws DbException;

	/**
	 * @return the current status of the mailbox.
	 */
	MailboxStatus getMailboxStatus(Transaction txn) throws DbException;

	/**
	 * Returns the currently running pairing task,
	 * or null if no pairing task is running.
	 */
	@Nullable
	MailboxPairingTask getCurrentPairingTask();

	/**
	 * Starts and returns a pairing task. If a pairing task is already running,
	 * it will be returned and the argument will be ignored.
	 *
	 * @param qrCodePayload The ISO-8859-1 encoded bytes of the mailbox QR code.
	 */
	MailboxPairingTask startPairingTask(String qrCodePayload);

	/**
	 * Takes a textual QR code representation in
	 * {@link ru.itis.messaging_engine.util.Base32} format and converts it
	 * into a qrCodePayload as expected by {@link #startPairingTask(String)}.
	 *
	 * @throws FormatException when the provided payload did not include a
	 * proper briar-mailbox:// link.
	 */
	String convertBase32Payload(String base32Payload) throws FormatException;

	/**
	 * Can be used by the UI to test the mailbox connection.
	 *
	 * @return true (success) or false (error).
	 * A {@link OwnMailboxConnectionStatusEvent} might be broadcast with a new
	 * {@link MailboxStatus}.
	 */
	boolean checkConnection();

	/**
	 * Unpairs the owner's mailbox and tries to wipe it.
	 * As this makes a network call, it should be run on the {@link IoExecutor}.
	 *
	 * @return true if we could wipe the mailbox, false if we couldn't.
	 * It is advised to inform the user to wipe the mailbox themselves,
	 * if we failed to wipe it.
	 */
	@IoExecutor
	boolean unPair() throws DbException;
}
