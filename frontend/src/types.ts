import {z} from 'zod'
export const valuesSchema=z.object({purity:z.number(),pressure:z.number(),temperature:z.number(),flow:z.number(),motorCurrent:z.number(),cycleCount:z.number(),outputCount:z.number(),running:z.boolean(),operatingMode:z.string(),alarmCode:z.string()})
export const latestSchema=z.object({machineId:z.string(),sequenceNumber:z.number(),recordedAt:z.string(),receivedAt:z.string(),values:valuesSchema})
export type Latest=z.infer<typeof latestSchema>
export type Telemetry={id:number;machineId:string;sequenceNumber:number;recordedAt:string;values:z.infer<typeof valuesSchema>}
export type Alarm={id:number;machineId:string;type:string;severity:'WARNING'|'CRITICAL';status:'ACTIVE'|'ACKNOWLEDGED'|'CLEARED';startTime:string;currentValue?:number;threshold?:number;acknowledgementUser?:string;clearedTime?:string}
export type Gateway={gatewayId:string;machineId:string;connected:boolean;lastSeen?:string}

