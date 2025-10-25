import { pgTable, serial, varchar, text, timestamp, integer, boolean } from "drizzle-orm/pg-core";

// Users Table
export const users = pgTable("users", {
  id: serial("id").primaryKey(),
  firstName: varchar("first_name", { length: 100 }).notNull(),
  lastName: varchar("last_name", { length: 100 }).notNull(),
  username: varchar("username", { length: 50 }).notNull().unique(),
  password: varchar("password", { length: 255 }).notNull(),
  role: varchar("role", { length: 20 }).notNull(), // Admin, Officer, User
  badgeNumber: varchar("badge_number", { length: 50 }),
  profilePhotoUri: text("profile_photo_uri"),
  isActive: boolean("is_active").default(true),
  profileCompleted: boolean("profile_completed").default(false),
  mustChangePassword: boolean("must_change_password").default(false),
  createdAt: timestamp("created_at").defaultNow(),
  updatedAt: timestamp("updated_at").defaultNow(),
});

// Blotter Reports Table
export const blotterReports = pgTable("blotter_reports", {
  id: serial("id").primaryKey(),
  caseNumber: varchar("case_number", { length: 50 }).notNull().unique(),
  incidentType: varchar("incident_type", { length: 100 }).notNull(),
  incidentDate: varchar("incident_date", { length: 50 }).notNull(),
  incidentTime: varchar("incident_time", { length: 50 }).notNull(),
  incidentLocation: text("incident_location").notNull(),
  narrative: text("narrative").notNull(),
  complainantName: varchar("complainant_name", { length: 200 }),
  complainantContact: varchar("complainant_contact", { length: 50 }),
  complainantAddress: text("complainant_address"),
  complainantEmail: varchar("complainant_email", { length: 100 }),
  status: varchar("status", { length: 50 }).notNull().default("Pending"),
  priority: varchar("priority", { length: 20 }).default("Normal"),
  assignedOfficer: varchar("assigned_officer", { length: 200 }),
  assignedOfficerIds: text("assigned_officer_ids"),
  filedBy: varchar("filed_by", { length: 200 }),
  filedById: integer("filed_by_id"),
  audioRecordingUri: text("audio_recording_uri"),
  isArchived: boolean("is_archived").default(false),
  createdAt: timestamp("created_at").defaultNow(),
  updatedAt: timestamp("updated_at").defaultNow(),
});

// Officers Table
export const officers = pgTable("officers", {
  id: serial("id").primaryKey(),
  name: varchar("name", { length: 200 }).notNull(),
  badgeNumber: varchar("badge_number", { length: 50 }).notNull().unique(),
  rank: varchar("rank", { length: 50 }),
  contactNumber: varchar("contact_number", { length: 50 }),
  email: varchar("email", { length: 100 }),
  userId: integer("user_id"),
  isActive: boolean("is_active").default(true),
  createdAt: timestamp("created_at").defaultNow(),
});

// Witnesses Table
export const witnesses = pgTable("witnesses", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  name: varchar("name", { length: 200 }).notNull(),
  contactNumber: varchar("contact_number", { length: 50 }),
  address: text("address"),
  statement: text("statement"),
  createdAt: timestamp("created_at").defaultNow(),
});

// Suspects Table
export const suspects = pgTable("suspects", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  name: varchar("name", { length: 200 }).notNull(),
  age: integer("age"),
  address: text("address"),
  description: text("description"),
  createdAt: timestamp("created_at").defaultNow(),
});

// Evidence Table
export const evidence = pgTable("evidence", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  evidenceType: varchar("evidence_type", { length: 100 }).notNull(),
  description: text("description").notNull(),
  locationFound: text("location_found"),
  photoUri: text("photo_uri"),
  collectedBy: varchar("collected_by", { length: 200 }),
  createdAt: timestamp("created_at").defaultNow(),
});

// Hearings Table
export const hearings = pgTable("hearings", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  hearingDate: varchar("hearing_date", { length: 50 }).notNull(),
  hearingTime: varchar("hearing_time", { length: 50 }).notNull(),
  location: text("location").notNull(),
  purpose: text("purpose"),
  status: varchar("status", { length: 50 }).default("Scheduled"),
  notes: text("notes"),
  createdAt: timestamp("created_at").defaultNow(),
});

// Resolutions Table
export const resolutions = pgTable("resolutions", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  resolutionType: varchar("resolution_type", { length: 100 }).notNull(),
  resolutionDetails: text("resolution_details").notNull(),
  resolvedDate: varchar("resolved_date", { length: 50 }).notNull(),
  resolvedBy: varchar("resolved_by", { length: 200 }),
  createdAt: timestamp("created_at").defaultNow(),
});

// Activity Logs Table
export const activityLogs = pgTable("activity_logs", {
  id: serial("id").primaryKey(),
  caseId: integer("case_id"),
  caseTitle: varchar("case_title", { length: 100 }),
  activityType: varchar("activity_type", { length: 100 }).notNull(),
  description: text("description").notNull(),
  oldValue: text("old_value"),
  newValue: text("new_value"),
  performedBy: varchar("performed_by", { length: 200 }).notNull(),
  timestamp: timestamp("timestamp").defaultNow(),
});

// Notifications Table
export const notifications = pgTable("notifications", {
  id: serial("id").primaryKey(),
  userId: integer("user_id").notNull(),
  title: varchar("title", { length: 200 }).notNull(),
  message: text("message").notNull(),
  type: varchar("type", { length: 50 }).notNull(),
  caseId: integer("case_id"),
  isRead: boolean("is_read").default(false),
  timestamp: timestamp("timestamp").defaultNow(),
});

// Persons Table
export const persons = pgTable("persons", {
  id: serial("id").primaryKey(),
  firstName: varchar("first_name", { length: 100 }).notNull(),
  lastName: varchar("last_name", { length: 100 }).notNull(),
  middleName: varchar("middle_name", { length: 100 }),
  contactNumber: varchar("contact_number", { length: 50 }),
  address: text("address"),
  personType: varchar("person_type", { length: 50 }).notNull(), // Complainant, Witness, Suspect, Respondent
  createdAt: timestamp("created_at").defaultNow(),
  updatedAt: timestamp("updated_at").defaultNow(),
});

// Person History Table (tracks person involvement in cases)
export const personHistory = pgTable("person_history", {
  id: serial("id").primaryKey(),
  personId: integer("person_id").notNull(),
  blotterReportId: integer("blotter_report_id").notNull(),
  role: varchar("role", { length: 50 }).notNull(), // Complainant, Witness, Suspect, Respondent
  notes: text("notes"),
  createdAt: timestamp("created_at").defaultNow(),
});

// Respondents Table
export const respondents = pgTable("respondents", {
  id: serial("id").primaryKey(),
  blotterReportId: integer("blotter_report_id").notNull(),
  name: varchar("name", { length: 200 }).notNull(),
  age: integer("age"),
  address: text("address"),
  contactNumber: varchar("contact_number", { length: 50 }),
  cooperationStatus: varchar("cooperation_status", { length: 50 }).default("Notified"), // Appeared, Notified, No Response
  createdAt: timestamp("created_at").defaultNow(),
});

// Respondent Statements Table
export const respondentStatements = pgTable("respondent_statements", {
  id: serial("id").primaryKey(),
  respondentId: integer("respondent_id").notNull(),
  blotterReportId: integer("blotter_report_id").notNull(),
  statement: text("statement").notNull(),
  dateGiven: varchar("date_given", { length: 50 }),
  takenBy: varchar("taken_by", { length: 200 }),
  createdAt: timestamp("created_at").defaultNow(),
});

// SMS Notifications Table
export const smsNotifications = pgTable("sms_notifications", {
  id: serial("id").primaryKey(),
  recipientName: varchar("recipient_name", { length: 200 }).notNull(),
  recipientNumber: varchar("recipient_number", { length: 50 }).notNull(),
  message: text("message").notNull(),
  messageType: varchar("message_type", { length: 50 }).notNull(), // Hearing, Status Update, etc.
  blotterReportId: integer("blotter_report_id"),
  deliveryStatus: varchar("delivery_status", { length: 50 }).default("Pending"), // Pending, Sent, Failed
  sentAt: timestamp("sent_at"),
  createdAt: timestamp("created_at").defaultNow(),
});

// Case Templates Table
export const caseTemplates = pgTable("case_templates", {
  id: serial("id").primaryKey(),
  templateName: varchar("template_name", { length: 200 }).notNull(),
  incidentType: varchar("incident_type", { length: 100 }).notNull(),
  templateContent: text("template_content").notNull(),
  createdBy: varchar("created_by", { length: 200 }),
  createdAt: timestamp("created_at").defaultNow(),
  updatedAt: timestamp("updated_at").defaultNow(),
});
