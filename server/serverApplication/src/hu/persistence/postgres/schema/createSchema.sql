/* user table */
CREATE TYPE "enumGender" AS ENUM ('male', 'female');/*eoq*/
CREATE TYPE "enumUserType" AS ENUM ('administrator', 'tutor', 'participant');/*eoq*/
CREATE TABLE "user" (
    "id" SERIAL PRIMARY KEY NOT NULL,
    "title" VARCHAR(50) NULL,
    "firstName" VARCHAR(50) NOT NULL,
    "lastName" VARCHAR(50) NOT NULL,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "gender" "enumGender" NULL,
    "birthday" DATE NULL,
    "street" VARCHAR(255) NULL,
    "zip" VARCHAR(20) NULL,
    "city" VARCHAR(255) NULL,
    "country" varchar(255) NULL,
    "phone" VARCHAR(100) NULL,
    "fax" VARCHAR(100) NULL,
    "website" VARCHAR(255) NULL,
    "passwordHash" VARCHAR(64) NOT NULL,
    "photoAvailable" BOOLEAN NOT NULL DEFAULT FALSE,
    "visible" BOOLEAN NOT NULL DEFAULT FALSE,
    "banned" BOOLEAN NOT NULL DEFAULT FALSE,
    "deletable" BOOLEAN NOT NULL DEFAULT TRUE,
    "type" "enumUserType" NOT NULL,
    "registered" DATE DEFAULT NOW(),
    "lastLogin" DATE NULL,
	"secretKey" varchar(50) NULL,
	"externUserId" varchar(50) NULL,
    CONSTRAINT "HuConstraint0001" CHECK ("deletable" OR (NOT "banned")),
    CONSTRAINT "HuConstraint0002" CHECK ("deletable" OR ("type" = 'administrator'))
);/*eoq*/

/* Trigger: don't change deletable flag, don't downgrade group owners */
CREATE FUNCTION trigfuncUserUpdate() RETURNS trigger AS $trigfuncUserUpdate$
    BEGIN
        IF OLD."type" <> NEW."type" AND NEW."type" = 'participant' AND (
                SELECT COUNT(*) FROM "userToGroup" WHERE "user" = OLD."id" AND "role" = 'owner'
        ) > 0 THEN
            RAISE EXCEPTION 'HuTriggerEx0002: The type of user % may not be changed to participant, because the user owns groups', OLD."id";
        END IF;

        RETURN NEW;
    END;
$trigfuncUserUpdate$ LANGUAGE plpgsql;/*eoq*/

CREATE TRIGGER "triggerUserUpdate"
BEFORE UPDATE ON "user"
FOR EACH ROW EXECUTE PROCEDURE trigfuncUserUpdate();/*eoq*/

/* Trigger: on delete -> is user deletable? */
CREATE FUNCTION trigfuncUserDelete() RETURNS trigger AS $trigfuncUserDelete$
    BEGIN
        IF NOT OLD."deletable" THEN
            RAISE EXCEPTION 'HuTriggerEx0003: User % is not deletable', OLD."id";
        END IF;
        RETURN OLD;
    END;
$trigfuncUserDelete$ LANGUAGE plpgsql;/*eoq*/

CREATE TRIGGER "triggerUserDelete"
BEFORE DELETE ON "user"
FOR EACH ROW EXECUTE PROCEDURE trigfuncUserDelete();/*eoq*/

CREATE FUNCTION trigfuncUserInsert() RETURNS trigger AS $trigfuncUserInsert$
    BEGIN
	   UPDATE "user" SET "secretKey" = CRYPT(CAST((SELECT RANDOM() * 2147483647 + 1) AS VARCHAR), gen_salt('md5')) WHERE "id" = NEW.id;
       RETURN NEW;
    END;
$trigfuncUserInsert$ LANGUAGE plpgsql;/*eoq*/

CREATE TRIGGER "triggerUserInsert"
AFTER INSERT ON "user"
FOR EACH ROW EXECUTE PROCEDURE trigfuncUserInsert();/*eoq*/

/* group table */
CREATE TABLE "group" (
    "id" SERIAL PRIMARY KEY NOT NULL,
    "title" VARCHAR(250) NOT NULL,
    "visible" BOOLEAN NOT NULL
);/*eoq*/


/* userToGroup table */
CREATE TYPE "enumUserGroupRole" AS ENUM ('attendant', 'owner');/*eoq*/
CREATE TABLE "userToGroup" (
    "user" INT NOT NULL REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    "group" INT NOT NULL REFERENCES "group"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    "role" "enumUserGroupRole" NOT NULL,

    PRIMARY KEY ("user", "group")
);/*eoq*/

/* Trigger: user is owner -> is user tutor/admin? */
CREATE FUNCTION trigfuncUserToGroupWrite() RETURNS trigger AS $trigfuncUserToGroupWrite$
    BEGIN
        IF NEW."role" = 'owner' AND (
            SELECT COUNT("id") FROM "user" 
            WHERE "id" = NEW."user"
            AND "type" IN ('tutor', 'administrator')
        ) = 0 THEN
            RAISE EXCEPTION 'HuTriggerEx0004: User % is unable to own an group', NEW."user";
        END IF;
        RETURN NEW;
    END;
$trigfuncUserToGroupWrite$ LANGUAGE plpgsql;/*eoq*/

CREATE TRIGGER "triggerUserToGroupWrite"
BEFORE INSERT OR UPDATE ON "userToGroup"
FOR EACH ROW EXECUTE PROCEDURE trigfuncUserToGroupWrite();/*eoq*/


/* video table */
CREATE TYPE "enumVideoRestriction" AS ENUM ('token', 'password', 'public', 'registered', 'groupAttendants', 'private');/*eoq*/
CREATE TABLE "video" (
    "id" SERIAL PRIMARY KEY NOT NULL,
    "group" INT NOT NULL REFERENCES "group"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    "title" varchar(250) NOT NULL,
    "description" TEXT NULL,
    "start" TIMESTAMP WITH TIME ZONE NULL,
    "stop" TIMESTAMP WITH TIME ZONE NULL,
    "participationRestriction" "enumVideoRestriction" NOT NULL,
    "password" VARCHAR(80) NULL,
    "directory" VARCHAR(32) NOT NULL,
    "version" INT DEFAULT '0',
    "ratingPoints" INT DEFAULT '0',
    "ratings" INT DEFAULT '0',
    "views" INT DEFAULT '0',
    "downloads" INT DEFAULT '0',
    "size" BIGINT DEFAULT '0',
    "author" INT NOT NULL REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE,
    "zipDownloadEnabled" BOOLEAN NOT NULL DEFAULT FALSE,
    "chromeAppURL" varchar(255) NULL,
    "created" TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    "lastUpdated" TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT "HuConstraint0003" CHECK (("start" IS NULL) OR ("stop" IS NULL) OR ("start" < "stop")),
    CONSTRAINT "HuConstraint0004" CHECK (("stop" IS NULL) OR ("start" IS NOT NULL))
);/*eoq*/

/* Trigger: disallow videos to be already started upon insert */
CREATE FUNCTION trigfuncVideoInsert() RETURNS trigger AS $trigfuncVideoInsert$
    BEGIN
        IF NEW."start" IS NOT NULL
            OR NEW."stop" IS NOT NULL
        THEN
            RAISE EXCEPTION 'HuTriggerEx0005: Video % may not be inserted, because its start or stop time is already populated', NEW."id";
        END IF;
        RETURN NEW;
    END;
$trigfuncVideoInsert$ LANGUAGE plpgsql;/*eoq*/

CREATE TRIGGER "triggerVideoInsert"
BEFORE INSERT ON "video"
FOR EACH ROW EXECUTE PROCEDURE trigfuncVideoInsert();/*eoq*/

/* token table */
CREATE TYPE "enumTokenType" AS ENUM ('participation', 'evaluation');/*eoq*/
CREATE TABLE "token" (
    "token" VARCHAR(32) PRIMARY KEY NOT NULL,
    "type" "enumTokenType" NOT NULL,
    "video" INT NOT NULL REFERENCES "video"("id") ON DELETE CASCADE ON UPDATE CASCADE

    CONSTRAINT "HuConstraint0010" CHECK (NOT ("type" = 'evaluation'))
);/*eoq*/

/* client table */
CREATE TABLE "client"(
	"id" INT PRIMARY KEY NOT NULL,
	"name" VARCHAR(255),
	"secret" VARCHAR(255)
);/*eoq*/

INSERT INTO "client" ("id", "name", "secret") VALUES (1, 'androidApp', 'a81c8b700951dbeadc38220b863a47eadc436eb4c31328a7c776e755dc0b86e9');/*eoq*/

/* oauthSession table */
CREATE TABLE "oauthSession"(
	"token" VARCHAR(255) PRIMARY KEY NOT NULL,
	"client" INT NOT NULL REFERENCES "client"("id") ON DELETE CASCADE ON UPDATE CASCADE,
	"user" INT NOT NULL REFERENCES "user"("id") ON DELETE CASCADE ON UPDATE CASCADE,
	"scope" VARCHAR(255) NOT NULL DEFAULT 'public',
	"expireDate" DATE NOT NULL
);/*eoq*/

/* logging tables */
CREATE TABLE "sivaPlayerSession"(
	"id" SERIAL PRIMARY KEY NOT NULL,
	"token" VARCHAR(255),
	"secondaryToken" VARCHAR(255),
	"user" INT REFERENCES "user"("id"),
	"video" INT REFERENCES "video"("id"),
	"videoVersion" INT NOT NULL,
	"start" TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
	"expireDate" DATE NOT NULL,
	"deleted" BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX "sivaPlayerSessionToken" ON "sivaPlayerSession"("id", "token");
/*eoq*/

CREATE TABLE "sivaPlayerLog"(
	"id" SERIAL PRIMARY KEY NOT NULL,
	"session" INT REFERENCES "sivaPlayerSession"("id") ON DELETE CASCADE ON UPDATE CASCADE,
	"time" TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
	"sceneTimeOffset" float default 0.0,
	"type" VARCHAR(255) NOT NULL,
	"element" varchar(255),
	"additionalInformation" varchar(255),
	"playerSequenceId" INT default '-1',
	"clientTime" BIGINT default 0,
	"originalSession" INT,
	"deleted" BOOLEAN NOT NULL DEFAULT FALSE
);/*eoq*/

CREATE VIEW "sivaPlayerLogByScene" AS SELECT l."id" AS "entry", l."session", s."user", s."video", s."videoVersion", (SELECT k."element" FROM "sivaPlayerLog" k WHERE k."session" = l."session" AND k."type" = 'loadScene' and k."id" <= l."id" ORDER BY k."id" DESC limit 1) AS "scene", l."type" AS "event", l."element", l."additionalInformation", l."sceneTimeOffset", l."time", l."clientTime", l."originalSession" FROM "sivaPlayerLog" l, "sivaPlayerSession" s WHERE l."session" = s."id" AND s."deleted" = false AND l.deleted = false/*eoq*/
CREATE VIEW "sivaPlayerLogMakroNavigation" AS SELECT l.* FROM "sivaPlayerLogByScene" l WHERE "event" IN ('loadScene', 'openTableOfContents', 'closeTableOfContents', 'openSearchArea', 'closeSearchArea', 'openFork', 'selectForkEntry', 'searchFor', 'selectSearchResult', 'selectTableOfContentsEntry', 'leaveVideo') OR ("event" = 'useButton' AND "element" IN ('next', 'back'));
CREATE VIEW "sivaPlayerLogVideoNavigation" AS SELECT l.* FROM "sivaPlayerLogByScene" l WHERE "event" IN ('clickVideo', 'selectTime') OR ("event" = 'useButton' AND "element" IN ('play', 'pause'));
CREATE VIEW "sivaPlayerLogAnnotation" AS SELECT l.* FROM "sivaPlayerLogByScene" l WHERE "event" IN ('openImageAnnotation', 'closeImageAnnotation', 'changeOpenedImage', 'clickMarkerAnnotation', 'manageMediaAnnotation', 'openAnnotationArea', 'closeAnnotationArea');
CREATE VIEW "sivaPlayerSessionDurationByClientTime" AS SELECT s."id" AS "session", s."user", to_char(to_timestamp(l."clientTime" / 1000), 'YYYY-MM-DD') AS "day", (max(l."clientTime") - min(l."clientTime")) AS "duration" FROM "sivaPlayerLog" l, "sivaPlayerSession" s WHERE l."session" = s."id" AND s."deleted" = false AND l.deleted = false GROUP BY s."id", s."user", "day";
CREATE VIEW "sivaPlayerSessionDurationByDayAndUser" AS SELECT "user", "day", SUM("duration") as "duration" FROM "sivaPlayerSessionDurationByClientTime" GROUP BY "user", "day";/*eoq*/

CREATE TYPE "enumCollaborationThreadVisibility" AS ENUM ('all', 'me', 'administrator');/*eoq*/
CREATE TABLE "collaborationThread"(
"id" SERIAL PRIMARY KEY NOT NULL,
"video" INT REFERENCES "video"("id") ON DELETE CASCADE ON UPDATE CASCADE,
"scene" VARCHAR(255) NOT NULL,
"title" VARCHAR(255) NOT NULL,
"durationFrom" INT NOT NULL,
"durationTo" INT NOT NULL,
"visibility" "enumCollaborationThreadVisibility" NOT NULL
);/*eoq*/

CREATE TABLE "collaborationPost"(
"id" SERIAL PRIMARY KEY NOT NULL,
"thread" INT REFERENCES "collaborationThread"("id") ON DELETE CASCADE ON UPDATE CASCADE,
"user" INT REFERENCES "user"("id"),
"date" TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
"post" TEXT,
"active" BOOLEAN NOT NULL DEFAUlT FALSE
);/*eoq*/

CREATE TABLE "collaborationMedia"(
"id" SERIAL PRIMARY KEY NOT NULL,
"post" INT REFERENCES "collaborationPost"("id") ON DELETE CASCADE ON UPDATE CASCADE,
"filename" VARCHAR(255)
);/*eoq*/