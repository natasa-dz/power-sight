import {User} from "./user.model";
import {AppointmentStatus} from "../enum/appointment-status";

export interface Appointment {
  employeeId: number,
  employeeUsername: string,
  userId: number,
  usersUsername: string,
  startTime: string,
  endTime: string,
  status: AppointmentStatus,
}
