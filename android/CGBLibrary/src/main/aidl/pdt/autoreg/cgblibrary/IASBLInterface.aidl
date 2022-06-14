// IASBLInterface.aidl
package pdt.autoreg.cgblibrary;

import pdt.autoreg.cgblibrary.screendefinitions.ScreenInfo;
import pdt.autoreg.cgblibrary.screendefinitions.ScreenNode;

interface IASBLInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    boolean clickByPos(int x, int y, boolean longPress);

    boolean clickByComp(String screenID, String compId);

    boolean swipe(int x1, int y1, int x2, int y2, int duration);

    boolean openPackage(String pckg);

    boolean inputText(String txt, in ScreenNode target, boolean delay);

    boolean scrollForward();

    boolean scrollBackward();

    boolean globalBack();

    String getCurrentForgroundPkg();

    void updateKeywordDefinitions();

    ScreenInfo detectScreen(String appName);
}