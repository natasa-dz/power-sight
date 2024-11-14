export enum Role {
  ADMIN = 'ADMIN',
  SUPERADMIN = 'SUPERADMIN',
  EMPLOYEE= 'EMPLOYEE',
  CITIZEN='CITIZEN',
  UNKNOWN = 'UNKNOWN',
}


export class User {
  username: string;
  password: string;
  role: Role;
  passwordChanged: boolean;  // Whether the user has changed their password
  active:boolean;
  userPhoto: string;
  activationToken: string;


  constructor(
    username: string,
    password: string,
    role: Role,
    passwordChanged: boolean,  // Whether the user has changed their password
    active:boolean,
    userPhoto: string,
    activationToken: string

  ) {

    this.username = username;
    this.password = password;
    this.role = role;
    this.passwordChanged = passwordChanged;
    this.active = active;
    this.userPhoto = userPhoto;
    this.activationToken = activationToken;
  }
}
