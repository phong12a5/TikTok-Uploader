#include "cloneinfo.h"
#include "log.h"

CloneInfo::CloneInfo(QJsonObject cloneInfo)
{
    m_cloneInfo = cloneInfo;
}

QString CloneInfo::cloneId()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_ID).toString();
}

QString CloneInfo::uid()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_UID).toString();
}

QString CloneInfo::appname()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_APPNAME).toString();
}

void CloneInfo::setPassWord(QString passWord)
{
    if(this->password() != passWord) {
        m_cloneInfo[CLONE_INFO_FIELD_PASSWORD] = passWord;
        emit cloneInfoChanged(CLONE_INFO_ACTION_UPDATE_PASSWORD);
    }
}

QString CloneInfo::password()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_PASSWORD).toString();
}

void CloneInfo::setSecretkey(QString secretkey)
{
    if(this->secretkey() != secretkey) {
        m_cloneInfo[CLONE_INFO_FIELD_SECRETKEY] = secretkey;
        emit cloneInfoChanged(CLONE_INFO_ACTION_UPDATE_SECRETKEY);
    }
}

QString CloneInfo::secretkey()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_SECRETKEY).toString();
}

QString CloneInfo::cookies()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_COOKIES).toString();
}

void CloneInfo::setAliveStatus(QString status, bool forceSync)
{
    if(this->aliveStatus() != status || forceSync) {
        m_cloneInfo[CLONE_INFO_FIELD_ALIVE_STATUS] = status;
        emit cloneInfoChanged(CLONE_INFO_ACTION_UPDATE_ALIVE_STATUS);
    }
}

QString CloneInfo::aliveStatus()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_ALIVE_STATUS).toString();
}

QString CloneInfo::userAgent()
{
    return m_cloneInfo.value(CLONE_INFO_FIELD_USER_AGENT).toString();
}

void CloneInfo::setUserAgent(QString userAgent)
{
    LOGD << userAgent;
    if(this->userAgent() != userAgent) {
        m_cloneInfo[CLONE_INFO_FIELD_USER_AGENT] = userAgent;
        emit cloneInfoChanged();
    }
}

QStringList CloneInfo::pageList()
{
    return m_pageList;
}

void CloneInfo::setPageList(QStringList pages)
{
    if(m_pageList != pages) {
        m_pageList = pages;
    }
}

QJsonObject CloneInfo::toJson()
{
    return m_cloneInfo;
}

QString CloneInfo::toString()
{
    QJsonDocument doc(m_cloneInfo);
    QString strJson(doc.toJson(QJsonDocument::Compact));
    return strJson;
}
