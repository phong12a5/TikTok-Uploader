#include "conversions.h"
#include "detail/shared.h"
#include "detail/error_handling.h"
#include "detail/types.h"
#include <algorithm>
#include <QDebug>

namespace webdriverxx {

inline
Client::Client(const std::string& url)
	: resource_(new detail::RootResource(
		url,
		detail::Shared<detail::IHttpClient>(new detail::HttpConnection)
		))
{}

inline
picojson::object Client::GetStatus() const {
	WEBDRIVERXX_FUNCTION_CONTEXT_BEGIN()
	const auto value = resource_->Get("status").get("value");
	WEBDRIVERXX_CHECK(value.is<picojson::object>(), "Value is not an object");
	return value.get<picojson::object>();
	WEBDRIVERXX_FUNCTION_CONTEXT_END()
}

inline
std::vector<Session> Client::GetSessions() const {
	WEBDRIVERXX_FUNCTION_CONTEXT_BEGIN()
	const auto sessions =
		FromJson<std::vector<detail::SessionRef>>(
			resource_->Get("sessions").get("value")
			);
	std::vector<Session> result;
	result.reserve(sessions.size());
	std::transform(sessions.begin(), sessions.end(), std::back_inserter(result),
		[this](const detail::SessionRef& session_ref) {
			return MakeSession(session_ref.id, detail::Resource::IsObserver);
		});
	return result;
	WEBDRIVERXX_FUNCTION_CONTEXT_END()
}

inline
Session Client::CreateSession(
	const Capabilities& desired,
	const Capabilities& required
	) const {
	WEBDRIVERXX_FUNCTION_CONTEXT_BEGIN()
#if 0
	picojson::array capabilities;
	capabilities.push_back(picojson::value(desired));

	picojson::object firstMatch;
	firstMatch.insert(std::make_pair("firstMatch", picojson::value(capabilities)));
#endif

	const auto response = resource_->Post("session",
		JsonObject()
			//.Set("desiredCapabilities", firstMatch) // don't set this, for some reason it will fuck up.
                          .Set("desiredCapabilities", static_cast<picojson::value>(desired))
                          .Set("requiredCapabilities", static_cast<picojson::value>(required))
		);
#if 0
        qDebug() << "response: " << response.serialize().c_str();
#endif
        const auto sessionId = response.get("sessionId");

	WEBDRIVERXX_CHECK(sessionId.is<std::string>(),                  "Session ID is not a string");
	WEBDRIVERXX_CHECK(response.get("value").is<picojson::object>(), "Capabilities is not an object");
	
	return MakeSession(sessionId.to_str(), detail::Resource::IsOwner);
	WEBDRIVERXX_FUNCTION_CONTEXT_END()
}

inline
Session Client::MakeSession(
	const std::string& id,
	detail::Resource::Ownership mode
	) const {
	return Session(detail::MakeSubResource(resource_, "session", id, mode));
}

} // namespace webdriverxx
