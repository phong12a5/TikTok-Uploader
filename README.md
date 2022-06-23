adb shell am broadcast -a GENERATE_CLONE_INFO  --es pacakge_name "com.zhiliaoapp.musically"
adb shell am broadcast -a BACKUP_PACKAGE  --es pacakge_name "com.zhiliaoapp.musically" --es username "username"
adb shell am broadcast -a USE_SSH_TUNNEL  --es hostname "us3.spotssh.us" --es username "spotssh.com-phong12a5" --es password "phong1994"