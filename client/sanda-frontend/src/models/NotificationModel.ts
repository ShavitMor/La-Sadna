export interface NotificationModel {
    message: string,
    id: number,
    date: string
}

export interface RequestModel extends NotificationModel {
    senderName: string,
    storeId: number,
    role: string
}