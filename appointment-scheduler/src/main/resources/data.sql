CREATE TABLE IF NOT EXISTS "doctor" (
	"id" varchar(255) NOT NULL,
	"email" varchar(255) NOT NULL,
	"name" varchar(255) NOT NULL,
	"specialization" varchar(255) NOT NULL,
	PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "patient" (
	"id" varchar(255) NOT NULL,
	"contact" int NOT NULL,
	"name" varchar(255) NOT NULL,
	"reason" varchar(255) NOT NULL,
	"age" int NOT NULL,
	"details" varchar(255) NOT NULL,
	PRIMARY KEY ("id", "contact")
);

CREATE TABLE IF NOT EXISTS "appointment" (
	"id" int NOT NULL,
	"patient_id" varchar(255) NOT NULL,
	"doctor_id" varchar(255) NOT NULL,
	"start_time" long NOT NULL,
	"end_time" long NOT NULL,
	"deleted" tinyint NOT NULL,
	PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "doctor_slots" (
	"slot_id" varchar(255) NOT NULL,
	"doctor_id" varchar(255) NOT NULL,
	"start_time" long NOT NULL,
	"end_time" long NOT NULL,
	"deleted" tinyint NOT NULL
);
