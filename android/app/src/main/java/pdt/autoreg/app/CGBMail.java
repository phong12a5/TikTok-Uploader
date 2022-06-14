package pdt.autoreg.app;

import com.chilkatsoft.CkEmail;
import com.chilkatsoft.CkEmailBundle;
import com.chilkatsoft.CkGlobal;
import com.chilkatsoft.CkImap;
import com.chilkatsoft.CkMessageSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pdt.autoreg.cgblibrary.LOG;

public class CGBMail {
    static {
        System.loadLibrary("chilkat");
    }

    private static String TAG = "CGBMail";



    public static boolean unlockChilkat() {
        CkGlobal glob = new CkGlobal();
        boolean success = glob.UnlockBundle("VONGTH.CB4082020_9kru5rnD5R2h");
        if (success != true) {
            LOG.E(TAG,glob.lastErrorText());
            return false;
        }

        int status = glob.get_UnlockStatus();
        if (status == 2) {
            LOG.D(TAG,"Unlocked using purchased unlock code.");
            return true;
        }
        else {
            LOG.E(TAG,"Unlocked in trial mode.");
        }

        // The LastErrorText can be examined in the success case to see if it was unlocked in
        // trial more, or with a purchased unlock code.
        LOG.E(TAG,glob.lastErrorText());
        return false;
    }

    public static String getTiktokCode(String email) {
        String code = null;
        LOG.D(TAG, String.format("email: %s", email));
        CkImap imap = new CkImap();

        imap.put_Port(993);
        imap.put_Ssl(true);

        boolean success = imap.Connect("imap.yandex.com");
        if (!success)
        {
            LOG.E(TAG, "imap.Connect: " + imap.lastErrorText());
            return code;
        }
        // Send the non-standard ID command...
        imap.sendRawCommand("ID (\"GUID\" \"1\")");
        if (!imap.get_LastMethodSuccess())
        {
            LOG.E(TAG, "imap.sendRawCommand: " + imap.lastErrorText());
            return code;
        }

        // Login
        success = imap.Login("admin@bobolala.xyz", "ecstipxneiopwyvx");
        if (!success) {
            LOG.E(TAG, "imap.Login: " + imap.lastErrorText());
            return "";
        }

        LOG.D(TAG, "Login Success!");

        //outlook: "Inbox"
        success = imap.SelectMailbox("Inbox");
        if (!success) {
            LOG.E(TAG, "imap.SelectMailbox: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "SelectMailbox success!");
        }

        // We can choose to fetch UIDs or sequence numbers.
        CkMessageSet messageSet;
        boolean fetchUids = true;
        // Get the message IDs of all the emails in the mailbox
        messageSet = imap.Search("ALL", fetchUids);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.Search: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "Search ALL mail box success!");
        }

        // Fetch the emails into a bundle object:
        CkEmailBundle bundle = imap.FetchBundle(messageSet);
        if (!imap.get_LastMethodSuccess()) {
            LOG.E(TAG, "imap.FetchBundle: " + imap.lastErrorText());
            return code;
        } else {
            LOG.D(TAG, "FetchBundle success!");
        }

        // Loop over the bundle and display the FROM and SUBJECT of each.
        int i = 0;
        int numEmails = bundle.get_MessageCount();
        while (i < numEmails) {
            CkEmail ckEmail = bundle.GetEmail(i);
            LOG.D(TAG, "email from: " + ckEmail.ck_from());
            LOG.D(TAG, "email to: " + ckEmail.getToAddr(0));
            LOG.D(TAG, "email subject: " + ckEmail.subject());

            if (ckEmail.ck_from().contains("TikTok")||
                    ckEmail.getToAddr(0).equals(email)) {
                LOG.D(TAG, "body: " + ckEmail.body());
                Pattern pattern = Pattern.compile("\\d{6,7}");
                Matcher matcher = pattern.matcher(ckEmail.subject());

                while (matcher.find()) {
                    code = matcher.group();
                    LOG.D(TAG, "code: " + code);
                }
                imap.SetFlag(ckEmail.GetImapUid(), true, "Deleted", 1);
            }
            i = i + 1;
        }

        // Expunge and close the mailbox.
        success = imap.ExpungeAndClose();

        // Disconnect from the IMAP server.
        success = imap.Disconnect();
        LOG.D(TAG, "code: " +  code);
        return code;
    }

}
