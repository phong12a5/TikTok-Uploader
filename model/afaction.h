#ifndef AFACTION_H
#define AFACTION_H

#include <QObject>

class AFAction : public QObject
{
    Q_OBJECT
public:
    explicit AFAction(QJsonObject actionJson, QObject* parent = nullptr);
    ~AFAction();

    enum ACTION_TYPE: int {
        E_FACEBOOK_ACTION_FEED = 0,
        E_FACEBOOK_ACTION_FEEDLIKE,
        E_FACEBOOK_ACTION_PAGESUB,

        E_FACEBOOK_ACTION_UNKNOWN
    };

    int action_type();
    QString action_type_str();
    QString fb_id();


    void setStatus(bool success, QString reason = "");

    QString toString();

private:
    QString m_service_code;
    int m_action_type;
    QString m_action_type_str;
    QString m_fb_id;
    int m_count;
    QString m_status;
    QString m_reason;

signals:

};

#endif // AFACTION_H
