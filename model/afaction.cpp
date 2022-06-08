#include "afaction.h"
#include <QJsonObject>
#include <QJsonDocument>

#define FACEBOOK_ACTION_FEED "Feed"
#define FACEBOOK_ACTION_FEEDLIKE "FeedLike"
#define FACEBOOK_ACTION_PAGESUB "PageSub"

AFAction::AFAction(QJsonObject actionJson, QObject* parent) : QObject(parent)
{
    m_service_code = actionJson.value("service_code").toString();
    m_fb_id = actionJson.value("fb_id").toString();
    m_count = actionJson.value("count").toInt();
    m_action_type_str = actionJson.value("action").toString();

    if(m_action_type_str == FACEBOOK_ACTION_FEED) m_action_type = E_FACEBOOK_ACTION_FEED;
    else if(m_action_type_str == FACEBOOK_ACTION_FEEDLIKE) m_action_type = E_FACEBOOK_ACTION_FEEDLIKE;
    else if(m_action_type_str == FACEBOOK_ACTION_PAGESUB) m_action_type = E_FACEBOOK_ACTION_PAGESUB;
    else m_action_type = E_FACEBOOK_ACTION_UNKNOWN;
}

AFAction::~AFAction()
{

}

int AFAction::action_type()
{
    return m_action_type;
}

QString AFAction::action_type_str()
{
    return m_action_type_str;
}

QString AFAction::fb_id()
{
    return m_fb_id;
}

void AFAction::setStatus(bool success, QString reason)
{
    if(success) {
        m_status = "Success";
    } else {
        m_status = "Report";
        m_reason = reason;
    }
}

QString AFAction::toString()
{
    QJsonObject json;
    json["service_code"] = m_service_code;
    json["action"] = m_action_type_str;
    json["fb_id"] = m_fb_id;
    json["count"] = m_count;
    json["status"] = m_status;
    json["reason"] = m_reason;
    return QString(QJsonDocument(json).toJson(QJsonDocument::Compact));
}
