import {City} from "./city.model";

export interface Municipality {
  id: number;
  name: string;
  city: City;
}
