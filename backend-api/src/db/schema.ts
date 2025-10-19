import { pgTable, uuid, varchar, text, timestamp, boolean, integer, date } from 'drizzle-orm/pg-core';
import { relations } from 'drizzle-orm';

// ========== USERS TABLE ==========
export const users = pgTable('users', {
  id: uuid('id').primaryKey().defaultRandom(),
  username: varchar('username', { length: 100 }).notNull().unique(),
  password: varchar('password', { length: 255 }).notNull(),
  role: varchar('role', { length: 20 }).notNull().default('User'), // User, Officer, Admin
  firstName: varchar('first_name', { length: 100 }).notNull(),
  lastName: varchar('last_name', { length: 100 }).notNull(),
  email: varchar('email', { length: 100 }),
  phone: varchar('phone', { length: 20 }),
  profilePicture: varchar('profile_picture', { length: 255 }),
  hasSelectedProfilePicture: boolean('has_selected_profile_picture').default(false),
  mustChangePassword: boolean('must_change_password').default(false),
  status: varchar('status', { length: 20 }).default('active'),
  createdAt: timestamp('created_at').defaultNow(),
  updatedAt: timestamp('updated_at').defaultNow()
});

// ========== OFFICERS TABLE ==========
export const officers = pgTable('officers', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').references(() => users.id).notNull(),
  badgeNumber: varchar('badge_number', { length: 50 }),
  rank: varchar('rank', { length: 50 }),
  specialization: varchar('specialization', { length: 100 }),
  assignedCasesCount: integer('assigned_cases_count').default(0),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== BLOTTER REPORTS TABLE ==========
export const blotterReports = pgTable('blotter_reports', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').references(() => users.id),
  blotterNumber: varchar('blotter_number', { length: 50 }).unique(),
  incidentType: varchar('incident_type', { length: 100 }).notNull(),
  incidentDate: timestamp('incident_date').notNull(),
  incidentTime: varchar('incident_time', { length: 10 }).notNull(),
  location: text('location').notNull(),
  description: text('description').notNull(),
  complainantName: varchar('complainant_name', { length: 200 }).notNull(),
  complainantContact: varchar('complainant_contact', { length: 20 }),
  complainantAddress: text('complainant_address'),
  status: varchar('status', { length: 50 }).default('Pending'),
  priority: varchar('priority', { length: 20 }).default('Normal'),
  assignedOfficerId: uuid('assigned_officer_id').references(() => officers.id),
  createdAt: timestamp('created_at').defaultNow(),
  updatedAt: timestamp('updated_at').defaultNow()
});

// ========== RESPONDENTS TABLE ==========
export const respondents = pgTable('respondents', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  personId: uuid('person_id'),
  name: varchar('name', { length: 200 }).notNull(),
  age: integer('age'),
  gender: varchar('gender', { length: 20 }),
  contact: varchar('contact', { length: 20 }),
  address: text('address'),
  cooperationStatus: varchar('cooperation_status', { length: 50 }),
  notes: text('notes'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== SUSPECTS TABLE ==========
export const suspects = pgTable('suspects', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  personId: uuid('person_id'),
  name: varchar('name', { length: 200 }).notNull(),
  alias: varchar('alias', { length: 100 }),
  age: integer('age'),
  gender: varchar('gender', { length: 20 }),
  contact: varchar('contact', { length: 20 }),
  address: text('address'),
  physicalDescription: text('physical_description'),
  evidenceFound: text('evidence_found'),
  status: varchar('status', { length: 50 }),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== WITNESSES TABLE ==========
export const witnesses = pgTable('witnesses', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  personId: uuid('person_id'),
  name: varchar('name', { length: 200 }).notNull(),
  age: integer('age'),
  gender: varchar('gender', { length: 20 }),
  contact: varchar('contact', { length: 20 }),
  address: text('address'),
  statement: text('statement'),
  reliability: varchar('reliability', { length: 50 }),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== EVIDENCE TABLE ==========
export const evidence = pgTable('evidence', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  type: varchar('type', { length: 50 }).notNull(),
  description: text('description').notNull(),
  filePath: varchar('file_path', { length: 500 }),
  collectedBy: varchar('collected_by', { length: 100 }),
  collectedDate: timestamp('collected_date'),
  storageLocation: varchar('storage_location', { length: 200 }),
  chainOfCustody: text('chain_of_custody'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== HEARINGS TABLE ==========
export const hearings = pgTable('hearings', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  hearingDate: date('hearing_date').notNull(),
  hearingTime: varchar('hearing_time', { length: 10 }).notNull(),
  location: varchar('location', { length: 200 }).notNull(),
  purpose: text('purpose'),
  attendees: text('attendees'),
  outcome: text('outcome'),
  notes: text('notes'),
  status: varchar('status', { length: 50 }).default('Scheduled'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== RESOLUTIONS TABLE ==========
export const resolutions = pgTable('resolutions', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  resolutionType: varchar('resolution_type', { length: 100 }).notNull(),
  details: text('details').notNull(),
  resolvedBy: varchar('resolved_by', { length: 100 }),
  resolutionDate: timestamp('resolution_date').defaultNow(),
  followUpRequired: boolean('follow_up_required').default(false),
  followUpDate: date('follow_up_date'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== RESPONDENT STATEMENTS TABLE ==========
export const respondentStatements = pgTable('respondent_statements', {
  id: uuid('id').primaryKey().defaultRandom(),
  respondentId: uuid('respondent_id').references(() => respondents.id).notNull(),
  statement: text('statement').notNull(),
  recordedBy: varchar('recorded_by', { length: 100 }),
  recordedDate: timestamp('recorded_date').defaultNow(),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== SMS NOTIFICATIONS TABLE ==========
export const smsNotifications = pgTable('sms_notifications', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id),
  respondentId: uuid('respondent_id').references(() => respondents.id),
  recipientName: varchar('recipient_name', { length: 200 }).notNull(),
  recipientPhone: varchar('recipient_phone', { length: 20 }).notNull(),
  message: text('message').notNull(),
  status: varchar('status', { length: 50 }).default('Pending'),
  sentAt: timestamp('sent_at'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== ACTIVITY LOGS TABLE ==========
export const activityLogs = pgTable('activity_logs', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').references(() => users.id),
  action: varchar('action', { length: 100 }).notNull(),
  entityType: varchar('entity_type', { length: 50 }),
  entityId: uuid('entity_id'),
  details: text('details'),
  ipAddress: varchar('ip_address', { length: 50 }),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== NOTIFICATIONS TABLE ==========
export const notifications = pgTable('notifications', {
  id: uuid('id').primaryKey().defaultRandom(),
  userId: uuid('user_id').references(() => users.id),
  title: varchar('title', { length: 200 }).notNull(),
  message: text('message').notNull(),
  type: varchar('type', { length: 50 }).notNull(),
  isRead: boolean('is_read').default(false),
  relatedEntityType: varchar('related_entity_type', { length: 50 }),
  relatedEntityId: uuid('related_entity_id'),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== PERSON HISTORY TABLE ==========
export const personHistory = pgTable('person_history', {
  id: uuid('id').primaryKey().defaultRandom(),
  personId: uuid('person_id').notNull(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  role: varchar('role', { length: 50 }).notNull(), // Respondent, Suspect, Witness
  name: varchar('name', { length: 200 }).notNull(),
  incidentType: varchar('incident_type', { length: 100 }),
  incidentDate: timestamp('incident_date'),
  outcome: varchar('outcome', { length: 100 }),
  createdAt: timestamp('created_at').defaultNow()
});

// ========== STATUS HISTORY TABLE ==========
export const statusHistory = pgTable('status_history', {
  id: uuid('id').primaryKey().defaultRandom(),
  reportId: uuid('report_id').references(() => blotterReports.id).notNull(),
  oldStatus: varchar('old_status', { length: 50 }),
  newStatus: varchar('new_status', { length: 50 }).notNull(),
  changedBy: uuid('changed_by').references(() => users.id),
  reason: text('reason'),
  createdAt: timestamp('created_at').defaultNow()
});
