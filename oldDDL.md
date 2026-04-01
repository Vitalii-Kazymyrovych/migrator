-- videoanalytics.alpr_list_items definition

CREATE TABLE `alpr_list_items` (
`id` int NOT NULL AUTO_INCREMENT,
`number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`status` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`created_by` int NOT NULL DEFAULT '0',
`closed_at` timestamp(3) NULL DEFAULT NULL,
`list_id` int DEFAULT NULL,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_lists definition

CREATE TABLE `alpr_lists` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`streams` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`send_internal_notifications` bit(1) NOT NULL DEFAULT b'0',
`events_holder` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`status` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`list_permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`enabled` bit(1) DEFAULT b'0',
`color` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#FFFFFF',
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_notifications definition

CREATE TABLE `alpr_notifications` (
`id` bigint NOT NULL AUTO_INCREMENT,
`plate_id` bigint NOT NULL,
`list_id` int DEFAULT NULL,
`list_item_id` int DEFAULT NULL,
`list_item_name` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
PRIMARY KEY (`id`),
KEY `alpr_notifications_plate_id` (`plate_id`),
KEY `alpr_notifications_list_item_id` (`plate_id`,`list_item_id`),
KEY `alpr_notifications_plate_id_status_list_item_name_created_at` (`plate_id`,`status`,`list_item_name`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=26654 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_plates definition

CREATE TABLE `alpr_plates` (
`id` bigint NOT NULL AUTO_INCREMENT,
`list_items` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`plate_number` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`arabic_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`adr` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`plate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`make_model_id` int NOT NULL DEFAULT '-1',
`vehicle_type` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL,
`color_id` int DEFAULT NULL,
`direction` int DEFAULT NULL,
`country` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`pattern` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`client_id` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`),
KEY `alpr_plates_created_at` (`created_at` DESC),
KEY `alpr_plates_created_at_stream` (`created_at` DESC,`stream_id`),
KEY `alpr_plates_created_at_plate_number` (`created_at` DESC,`plate_number`),
KEY `alpr_plates_created_at_make_model` (`created_at` DESC,`make_model_id`),
KEY `alpr_plates_created_at_stream_make_model` (`created_at` DESC,`make_model_id`,`stream_id`),
KEY `alpr_plates_created_at_color_and_make_model` (`created_at` DESC,`make_model_id`,`color_id`),
KEY `alpr_plates_plate_number` (`plate_number`),
KEY `alpr_plates_stream_id` (`stream_id`),
KEY `alpr_plates_plate_number_stream_id` (`plate_number`,`stream_id`),
KEY `alpr_plates_id_plate_number` (`plate_number`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1098888 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_speed_rule_events definition

CREATE TABLE `alpr_speed_rule_events` (
`id` int NOT NULL AUTO_INCREMENT,
`rule_id` int DEFAULT NULL,
`plate_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`make_model_id` int DEFAULT '-1',
`color_id` int DEFAULT NULL,
`direction` int DEFAULT NULL,
`country` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`country_pattern` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`stream1_frame` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream2_frame` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream1_timestamp` timestamp(3) NULL DEFAULT NULL,
`stream2_timestamp` timestamp(3) NULL DEFAULT NULL,
`speed_limit` int DEFAULT '0',
`speed_unit` int DEFAULT '0',
`client_id` int NOT NULL,
PRIMARY KEY (`id`),
KEY `alpr_speed_rule_events_rule_id` (`rule_id`),
KEY `alpr_speed_rule_events_rule_id_timestamp` (`stream2_timestamp` DESC,`rule_id`),
KEY `alpr_speed_rule_events_client_id` (`client_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_speed_rules definition

CREATE TABLE `alpr_speed_rules` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`stream_id1` int NOT NULL,
`stream_id2` int NOT NULL,
`speed_limit` int DEFAULT '0',
`speed_unit` int DEFAULT '0',
`distance` double DEFAULT '0',
`distance_unit` int DEFAULT '0',
`min_speed` int DEFAULT '0',
`min_speed_unit` int DEFAULT '0',
`max_duration` int DEFAULT '0',
`events_holder` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.alpr_stats_hourly definition

CREATE TABLE `alpr_stats_hourly` (
`id` int NOT NULL AUTO_INCREMENT,
`stream_id` int NOT NULL,
`total` int NOT NULL,
`numbers` int NOT NULL,
`make_models` int NOT NULL,
`created_at` timestamp(3) NOT NULL,
`client_id` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `alpr_stats_hourly_created_at` (`created_at`,`client_id`),
KEY `alpr_stats_hourly_created_at_stream` (`created_at`,`stream_id`,`client_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7988 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.analytics definition

CREATE TABLE `analytics` (
`id` int NOT NULL AUTO_INCREMENT,
`topic` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`type` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`plugin_name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`created_at` timestamp(3) NOT NULL,
`status` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`client_id` int NOT NULL,
`stream` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`module` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`last_gpu_id` int DEFAULT NULL,
`desired_server_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`disable_balancing` bit(1) DEFAULT NULL,
`start_signature` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`allowed_server_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream_id` int DEFAULT NULL,
`events_holder` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`start_at` timestamp(3) NULL DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=672 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.audit_trail definition

CREATE TABLE `audit_trail` (
`id` int NOT NULL AUTO_INCREMENT,
`created_at` timestamp(3) NOT NULL,
`session_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`user_id` int NOT NULL,
`user_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`source_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`event_category` int NOT NULL,
`event_action` int NOT NULL,
`stream_id` int DEFAULT NULL,
`analytics_id` int DEFAULT NULL,
`message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12973 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.clients definition

CREATE TABLE `clients` (
`id` int NOT NULL AUTO_INCREMENT,
`client_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`country` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`city` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`address` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`zip_code` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`email` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.event_manager definition

CREATE TABLE `event_manager` (
`id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`created_at` timestamp(3) NULL DEFAULT NULL,
`nodes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.face_list_items definition

CREATE TABLE `face_list_items` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`status` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`created_by` int NOT NULL DEFAULT '0',
`closed_at` timestamp(3) NULL DEFAULT NULL,
`list_id` int DEFAULT NULL,
`expiration_settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.face_list_items_images definition

CREATE TABLE `face_list_items_images` (
`id` int NOT NULL AUTO_INCREMENT,
`list_item_id` int NOT NULL,
`path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`encoding` blob,
`points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
PRIMARY KEY (`id`),
KEY `list_item_id` (`list_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.face_lists definition

CREATE TABLE `face_lists` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`min_confidence` int NOT NULL DEFAULT '80',
`streams` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`send_internal_notifications` bit(1) NOT NULL DEFAULT b'0',
`events_holder` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`status` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`client_id` int NOT NULL,
`color` char(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#FFFFFF',
`time_attendance` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`enabled` bit(1) DEFAULT b'0',
`list_permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.gender_age_stat definition

CREATE TABLE `gender_age_stat` (
`id` int NOT NULL AUTO_INCREMENT,
`va_id` int NOT NULL,
`stream_id` int NOT NULL,
`gender` int NOT NULL,
`age` int NOT NULL,
`count` int NOT NULL,
`date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`client_id` int NOT NULL,
PRIMARY KEY (`id`),
KEY `date_gender_age` (`date`),
KEY `date_va_id_gender_age` (`date`,`va_id`)
) ENGINE=InnoDB AUTO_INCREMENT=854165 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.gun_notifications definition

CREATE TABLE `gun_notifications` (
`id` bigint NOT NULL AUTO_INCREMENT,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`thumbnail_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`objects` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`zone` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`created_at` timestamp(3) NOT NULL,
`client_id` int NOT NULL,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`),
KEY `gun_detection_id_status_created_at` (`id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=7049 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.gun_type_mapping definition

CREATE TABLE `gun_type_mapping` (
`id` bigint NOT NULL AUTO_INCREMENT,
`notification_id` bigint NOT NULL,
`type` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7049 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.hardhats_notifications definition

CREATE TABLE `hardhats_notifications` (
`id` int NOT NULL AUTO_INCREMENT,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
`objects` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`thumbnail_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`zone` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`client_id` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `hardhats_id_status_created_at` (`id`,`status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=31873 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.object_in_zone_notifications definition

CREATE TABLE `object_in_zone_notifications` (
`id` int NOT NULL AUTO_INCREMENT,
`va_id` int DEFAULT NULL,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
`stream_id` int NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`thumbnail_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`zone` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`dwell_time` int NOT NULL,
`trigger` int NOT NULL,
`notification_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'alert',
`action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`resolution` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`created_at` timestamp(3) NOT NULL,
`client_id` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `id_status_created_at` (`id`,`status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=171966 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.port_logistics_container_numbers definition

CREATE TABLE `port_logistics_container_numbers` (
`id` int NOT NULL AUTO_INCREMENT,
`number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`iso` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`number_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`detection_id` int NOT NULL,
`recognized_at` timestamp(3) NULL DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.port_logistics_detections definition

CREATE TABLE `port_logistics_detections` (
`id` int NOT NULL AUTO_INCREMENT,
`truck_number` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`truck_arabic_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`truck_box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`truck_plate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`truck_frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`trailer_number` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`trailer_arabic_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`trailer_box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`trailer_plate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`trailer_frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`adr` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`pattern` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`country` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`state` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`make_model_id` int NOT NULL DEFAULT '-1',
`vehicle_type` int NOT NULL DEFAULT '1',
`color_id` int DEFAULT NULL,
`client_id` int NOT NULL DEFAULT '0',
`rule_id` int NOT NULL,
`created_at` timestamp(3) NOT NULL,
`front_plate_recognized_at` timestamp(3) NOT NULL,
`trailer_plate_recognized_at` timestamp(3) NULL DEFAULT NULL,
`back_plate_number` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`back_plate_arabic_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`back_plate_box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`back_plate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`back_plate_frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`back_plate_recognized_at` timestamp(3) NULL DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.port_logistics_rules definition

CREATE TABLE `port_logistics_rules` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`front_lpr_analytics_id` int NOT NULL,
`back_lpr_analytics_id` int DEFAULT NULL,
`container_analytics_ids` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`created_at` timestamp(3) NOT NULL,
`start_at` timestamp(3) NULL DEFAULT NULL,
`status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'stopped',
`client_id` int NOT NULL,
`buffer_time` int DEFAULT '20',
`buffet_time` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.railroad_numbers definition

CREATE TABLE `railroad_numbers` (
`id` int NOT NULL AUTO_INCREMENT,
`number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`number_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream_id` int NOT NULL,
`created_at` timestamp(3) NOT NULL,
`direction` smallint DEFAULT NULL,
`client_id` int DEFAULT NULL,
`iso_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `railroad_numbers_created_at` (`created_at` DESC),
KEY `railroad_numbers_created_at_stream` (`created_at` DESC,`stream_id`),
KEY `railroad_numbers_created_at_number` (`created_at` DESC,`number`)
) ENGINE=InnoDB AUTO_INCREMENT=749358 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.roles definition

CREATE TABLE `roles` (
`id` int NOT NULL AUTO_INCREMENT,
`role_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`permissions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.servers definition

CREATE TABLE `servers` (
`id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.settings definition

CREATE TABLE `settings` (
`Variable_name` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`Value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
PRIMARY KEY (`Variable_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.smoke_fire_notifications definition

CREATE TABLE `smoke_fire_notifications` (
`id` bigint NOT NULL AUTO_INCREMENT,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
`objects` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`thumbnail_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`zone` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`client_id` int DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `smoke_fire_id_status_created_at` (`id`,`status`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=9229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.smoke_fire_type_mapping definition

CREATE TABLE `smoke_fire_type_mapping` (
`id` bigint NOT NULL AUTO_INCREMENT,
`notification_id` bigint NOT NULL,
`type` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.stats_traffic_hourly definition

CREATE TABLE `stats_traffic_hourly` (
`id` bigint NOT NULL AUTO_INCREMENT,
`va_id` int NOT NULL,
`line` int NOT NULL,
`type` int NOT NULL,
`count` int NOT NULL,
`direction` int NOT NULL DEFAULT '0',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`client_id` int NOT NULL,
`present` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.stats_traffic_minutely definition

CREATE TABLE `stats_traffic_minutely` (
`id` bigint NOT NULL AUTO_INCREMENT,
`va_id` int NOT NULL,
`line` int NOT NULL,
`type` int NOT NULL,
`count` int NOT NULL,
`direction` int NOT NULL DEFAULT '0',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`client_id` int NOT NULL,
`present` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`),
KEY `stats_traffic_hourly_created_at_client` (`created_at`,`client_id`),
KEY `stats_traffic_hourly_created_at_va_id` (`created_at`,`va_id`,`client_id`),
KEY `stats_traffic_minutely_created_at_client` (`created_at`,`client_id`),
KEY `stats_traffic_minutely_created_at_va_id` (`created_at`,`va_id`,`client_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57794 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.stream_groups definition

CREATE TABLE `stream_groups` (
`id` int NOT NULL AUTO_INCREMENT,
`parent_id` int NOT NULL,
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`client_id` int NOT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.streams definition

CREATE TABLE `streams` (
`id` int NOT NULL AUTO_INCREMENT,
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`path` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`width` int NOT NULL,
`height` int NOT NULL,
`file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`status` tinyint NOT NULL,
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`lat` double NOT NULL DEFAULT '0',
`lng` double NOT NULL DEFAULT '0',
`type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'rtsp',
`uuid` varchar(55) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`auth` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`direction` int NOT NULL DEFAULT '0',
`client_id` int NOT NULL,
`codec` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`timezone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`duration` bigint DEFAULT NULL,
`restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`parent_id` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.traffic_lights_detections definition

CREATE TABLE `traffic_lights_detections` (
`id` int NOT NULL AUTO_INCREMENT,
`direction` int DEFAULT NULL,
`line_direction` int DEFAULT NULL,
`box` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`line` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`plate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`plate_number` varchar(9) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`make_model_id` int NOT NULL DEFAULT '-1',
`vehicle_type` int NOT NULL DEFAULT '1',
`country` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`country_pattern` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`state` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`color_id` tinyint DEFAULT NULL,
`created_at` timestamp(3) NOT NULL,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`client_id` int DEFAULT NULL,
`status` int NOT NULL DEFAULT '1',
`accepted_by` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.traffic_stat definition

CREATE TABLE `traffic_stat` (
`id` bigint NOT NULL AUTO_INCREMENT,
`stream_id` int NOT NULL,
`va_id` int NOT NULL,
`line` int NOT NULL,
`type` int NOT NULL,
`count` int NOT NULL,
`direction` int NOT NULL DEFAULT '0',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`client_id` int NOT NULL,
`frame_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`object_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`notification_status` int NOT NULL DEFAULT '0',
`accepted_by` int NOT NULL DEFAULT '0',
PRIMARY KEY (`id`),
KEY `traffic_stat_type` (`type`),
KEY `va_id` (`va_id`),
KEY `line` (`line`),
KEY `traffic_stat_notification_status_index` (`notification_status`)
) ENGINE=InnoDB AUTO_INCREMENT=155995 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.users definition

CREATE TABLE `users` (
`id` int NOT NULL AUTO_INCREMENT,
`email` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`fullname` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
`last_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
`last_login` timestamp(3) NOT NULL,
`ip_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`role_id` int NOT NULL DEFAULT '0',
`status` int NOT NULL DEFAULT '1',
`created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
`settings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`client_id` int NOT NULL,
`client_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`type` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'basic',
`timezone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.zone_exit_notifications definition

CREATE TABLE `zone_exit_notifications` (
`id` int NOT NULL AUTO_INCREMENT,
`va_id` int NOT NULL,
`stream_id` int NOT NULL,
`object_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
`zone_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
`seconds_in_zone` int NOT NULL,
`object_type` smallint DEFAULT NULL,
`notification_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'exit',
`created_at` timestamp(3) NOT NULL,
`client_id` int DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.zone_exit_notifications_object_type definition

CREATE TABLE `zone_exit_notifications_object_type` (
`id` int NOT NULL AUTO_INCREMENT,
`zone_exit_notifications_id` int NOT NULL DEFAULT '1',
`object_type` int DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- videoanalytics.object_in_zone_object_type definition

CREATE TABLE `object_in_zone_object_type` (
`id` int NOT NULL AUTO_INCREMENT,
`object_in_zone_id` int NOT NULL DEFAULT '1',
`object_type` int DEFAULT NULL,
`confidence` float DEFAULT NULL,
`box` blob,
PRIMARY KEY (`id`),
KEY `object_in_zone_object_type_object_in_zone_id_index` (`object_in_zone_id`),
CONSTRAINT `FK__object_in_zone_object_type_object_in_zone_id` FOREIGN KEY (`object_in_zone_id`) REFERENCES `object_in_zone_notifications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=290053 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;