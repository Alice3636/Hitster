# README

## תיאור כללי
מסמך זה מתאר את הצעדים הדרושים כדי להריץ את המערכת בצורה מקומית (Localhost) לצורכי בדיקה ופיתוח.

המערכת מחולקת לשני פרויקטים:

- **Backend (Server)** – צד השרת המבוסס על Spring Boot
- **Frontend (Client)** – צד הלקוח

---

## Features / תכונות מרכזיות

- שרת RESTful מבוסס Spring Boot
- ממשק משתמש Client נפרד
- חיבור למסד נתונים לניהול נתוני המערכת
- מערכת התחברות והרשמת משתמשים
- תקשורת בין Client ל-Server באמצעות API

---

## Tech Stack

- **Java** –  להשתמש בגרסה 17
- **Spring Boot** – צד שרת
- **Database** – MySQL
- **Build Tool** – Maven
- **IDE** – IntelliJ IDEA / Eclipse / VScode

---

## דרישות מקדימות (Prerequisites)

לפני תחילת ההתקנה, ודאו כי קיימים הרכיבים הבאים:

- `JDK 17`
- מסד נתונים פעיל (`MySQL 8.0`)
- `Maven`
- IDE תומך ב־ Java

ניתן לבדוק את גרסת Java באמצעות:

```bash
java -version
```

וניתן לבדוק את גרסת Maven באמצעות:

```bash
mvn -version
```

---

## הפעלת השרת (Backend / Server)

### 1. הגדרת קובץ התצורה

לפני הרצת השרת, פתחו את קובץ ההגדרות:

```text
src/main/resources/config.yml
```

ודאו כי פרטי ההתחברות למסד הנתונים מעודכנים:

- URL של מסד הנתונים
- שם משתמש
- סיסמה



---

### 2. הרצת השרת

הפעילו את מחלקת ההרצה הראשית של Spring Boot:

```text
HitsterApplication
```

או באמצעות Maven:

```bash
mvn spring-boot:run
```

לאחר ההרצה, השרת יעלה ויאזין לבקשות נכנסות בכתובת:

```text
http://localhost:8080/api
```

---

## הפעלת הלקוח (Frontend / Client)

לאחר שהשרת רץ ללא שגיאות:

1. נווטו לפרויקט הלקוח (`Client`)
2. הפעילו את מחלקת ההרצה:

```text
AppLauncher
```

---

## שימוש ראשוני במערכת

לאחר פתיחת האפליקציה:

1. יופיע מסך התחברות
2. בשימוש ראשון – לחצו על `Register` 
3. צרו משתמש חדש
4. התחברו למערכת
5. התחילו להשתמש באפליקציה

---


## הערות

- יש לוודא שהשרת פועל לפני הפעלת הלקוח
- מומלץ לבדוק שהחיבור למסד הנתונים תקין לפני ההרצה
- במקרה של שגיאת חיבור, יש לבדוק את ערכי `config.yml`
- מומלץ לבצע Build נקי לפני ההפעלה:

```bash
mvn clean install
```

---

## מבנה כללי של הפרויקט

```text
Project Root
│
├── Backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── config.yml
│   └── pom.xml
│
├── Frontend/
│   ├── src/main/java/
│   └── pom.xml
│
└── README.md
```

