#QT +=   quick
QT += widgets
QT += core gui qml quick
QT += sql xml
QT += network

#CONFIG += c++11 console
CONFIG += c++11
CONFIG -= app_bundle

# The following define makes your compiler emit warnings if you use
# any Qt feature that has been marked deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if it uses deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0
SOURCES += \
        appmodel.cpp \
        log.cpp \
        main.cpp \
        appmain.cpp \
        model/afaction.cpp \
        model/cloneinfo.cpp \
        model/servicedata.cpp \
        service/baseservice.cpp \
        service/chromeservice.cpp \
        service/servicemanager.cpp \
        service/apiservices.cpp \
        utils.cpp

HEADERS += \
    AppDefine.h \
    AppEnum.h \
    DefineString.h \
    appmain.h \
    appmodel.h \
    log.h \
    model/afaction.h \
    model/cloneinfo.h \
    model/servicedata.h \
    service/baseservice.h \
    service/chromeservice.h \
    service/servicemanager.h \
    service/apiservices.h \
    utils.h

INCLUDEPATH += $$PWD/webdriverxx/src/include
INCLUDEPATH += $$PWD/chilkat/include
LIBS += -L$$PWD/chilkat/lib -lchilkat-9.5.0 -lcurl


#copy chromedriver.exe to output folder
#!exists( $$OUT_PWD/chromedriver.exe) {
#    QMAKE_POST_LINK += copy /y "$$shell_path($$PWD/chromedriver.exe)" "$$shell_path($$OUT_PWD)"
#}

RC_ICONS = Logo.ico

# Default rules for deployment.
qnx: target.path = /tmp/$${TARGET}/bin
else: unix:!android: target.path = /opt/$${TARGET}/bin
!isEmpty(target.path): INSTALLS += target

RESOURCES += \
    qml.qrc

DISTFILES += \
    model/definitions.json

