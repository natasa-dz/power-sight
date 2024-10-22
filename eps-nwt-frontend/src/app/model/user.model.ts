export enum Role {
  ADMIN = 'ADMIN',
  SUPERADMIN = 'SUPERADMIN',
  EMPLOYEE= 'EMPLOYEE',
  CITIZEN='CITIZEN',
  UNKNOWN = 'UNKNOWN',
}
export enum UserStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE'
}

export class User {
  username: string;
  password: string;
  role: Role;
  confirmPassword:string;
  status: UserStatus;


  constructor(
    email: string,
    password: string,
    role: Role,
    confirm:string,
    status: UserStatus,

  ) {

    this.username = email;
    this.password = password;
    this.role = role;
    this.confirmPassword=confirm;
    this.status = status;
  }
}
