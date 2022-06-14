package pdt.autoreg.app;

import com.chilkatsoft.CkJsonObject;
import com.chilkatsoft.CkRest;
import com.chilkatsoft.CkStream;

import pdt.autoreg.cgblibrary.LOG;

public class DropboxAPI {
    private static final String TAG = "DropboxAPI";
    private static String TOKEN_P1 = "lr7usq7SigAAAAAAAAAAZ74c-";
    private static String TOKEN_P2 = "zhZd2jOrNZLkp15x1JMx4uSgvLRlMIrXlNoYVQN";

    public static boolean downloadFileFromDropbox(String pathFile, String localPath) {
        LOG.D(TAG, "pathFile: " + pathFile + " -- savePath: " + localPath);
        CkRest rest = new CkRest();
        rest.put_IdleTimeoutMs(120000);

        //  Connect to Dropbox
        if (!rest.Connect("content.dropboxapi.com", 443, true, true))
        {
            LOG.D(TAG,"Connect error: " + rest.lastErrorText());
            return false;
        }
        
        //  Add request headers.
        String token = "Bearer " + TOKEN_P1 + TOKEN_P2;
        rest.AddHeader("Authorization", token);

        CkJsonObject json = new CkJsonObject();
        json.AppendString("path", pathFile);
        rest.AddHeader("Dropbox-API-Arg", json.emit());

        CkStream fileStream = new CkStream();
        fileStream.put_SinkFile(localPath);

        int expectedStatus = 200;
        rest.SetResponseBodyStream(expectedStatus, true, fileStream);

        String responseStr = rest.fullRequestNoBody("POST", "/2/files/download");
        if (!rest.get_LastMethodSuccess())
        {
            LOG.E(TAG,"responseStr error: " + rest.lastErrorText());
            return false;
        }
        else
        {
            LOG.E(TAG,"responseStr: " + responseStr);
        }

        //  When successful, Dropbox responds with a 200 response code.
        if (rest.get_ResponseStatusCode() != 200)
        {
            //  Examine the request/response to see what happened.
            LOG.D(TAG,"response status code = %d" + rest.get_ResponseStatusCode());
            LOG.D(TAG,"response status text = " + rest.responseStatusText());
            LOG.D(TAG,"response header: " + rest.responseHeader());
            LOG.D(TAG,"response body (if any): " + responseStr);
            LOG.D(TAG,"LastRequestStartLine: " + rest.lastRequestStartLine());
            LOG.D(TAG,"LastRequestHeader: " + rest.lastRequestHeader());
            LOG.D(TAG,"lastErrorText: " + rest.lastErrorText());
            return false;
        }
        LOG.D("Download %s successful", pathFile);
        return true;
    }

}
