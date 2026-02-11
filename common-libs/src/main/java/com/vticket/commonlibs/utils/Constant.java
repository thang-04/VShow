package com.vticket.commonlibs.utils;

public class Constant {
    public static final class RedisKey {
        public static final String REDIS_LIST_CATEGORY = "vt:list:cate";
        public static final String REDIS_CATEGORY_BY_ID = "vt:cate:%s";
        public static final String ACCESS_TOKEN = "vt:payment:token:";
        public static final String USER_ID = "vt:user:id:";
        public static final String USER_USERNAME = "vt:user:username:";
        public static final String ALL_USERS = "vt:users:all";
        public static final String INTROSPECT_TOKEN = "vt:token:introspect:";
        public static final String USER_TYPE_LOGIN = "vt:users:";
        public static final String OTP_EMAIL = "vt:otp:email:%s";
        public static final String PENDING_USER_EMAIL = "vt:pending:user:%s";
        public static final String REDIS_EVENT_BY_CATEGORY_ID = "vt:list:event:cate:%s";
        public static final String REDIS_LIST_EVENT = "vt:list:event:%s";
        public static final String REDIS_EVENT_BY_ID = "vt:event:%s";
        public static final String SEAT_LOCK = "vt:seat:lock:";
        public static final String SEAT_HOLD = "vt:seat:hold:%s:%s";
        public static final String SEAT_STATUS = "vt:seat:status:event:%s";
        public static final String BLACK_LIST_TOKEN = "vt:black:list:token:%s";
    }

    public static final class KAFKA {
        // Email Events
        public static final String TOPIC_EMAIL_LOGIN = "email-login-events";
        public static final String TOPIC_EMAIL_TICKET = "email-ticket-events";

        // Consumer Group
        public static final String GROUP_EMAIL_SERVICE = "vticket-email-service";
    }

    public static final class RABBITMQ {
        // EXCHANGE DÙNG CHUNG
        public static final String DIRECT_EXCHANGE = "email.direct.exchange";
        public static final String TOPIC_EXCHANGE = "email.topic.exchange";
        public static final String FANOUT_EXCHANGE = "email.fanout.exchange";
        public static final String DLX_EXCHANGE = "email.dlx.exchange";// gom tất cả message fail
        public static final String DLQ_QUEUE = "email.dlq.queue";

        // DIRECT QUEUE
        public static final String QUEUE_OTP_MAIL = "mail.otp.queue";
        public static final String ROUTING_OTP_MAIL = "mail.otp.routing";

        public static final String QUEUE_TICKET_MAIL = "mail.ticket.queue";
        public static final String ROUTING_TICKET_MAIL = "mail.ticket.routing";

        // MAIL FIRST_LOGIN MODULE
        public static final String QUEUE_MAIL = "mail.queue";
        public static final String EXCHANGE_MAIL = "mail.exchange";
        public static final String ROUTING_MAIL = "mail.routing";

        // MAIL CONFIRM TICKET
        public static final String QUEUE_MAIL_TICKET = "mail.ticket.queue";
        public static final String EXCHANGE_MAIL_TICKET = "mail.ticket.exchange";
        public static final String ROUTING_MAIL_TICKET = "mail.ticket.routing";

        // PAYMENT MODULE
        public static final String QUEUE_PAYMENT = "payment.queue";
        public static final String EXCHANGE_PAYMENT = "payment.exchange";
        public static final String ROUTING_PAYMENT = "payment.routing";

        // TICKET MODULE
        public static final String QUEUE_TICKET = "ticket.queue";
        public static final String EXCHANGE_TICKET = "ticket.exchange";
        public static final String ROUTING_TICKET = "ticket.routing";

        // TOPIC QUEUE
        public static final String QUEUE_TOPIC_USER = "mail.user.topic.queue";
        public static final String QUEUE_TOPIC_ADMIN = "mail.admin.topic.queue";

        // FANOUT QUEUE
        public static final String QUEUE_FANOUT = "mail.broadcast.queue";

    }
}
