import {NativeModules} from 'react-native';

interface CalendarInterface {
    createCalendarEvent(name: string, location: string): void;
    getConstants(): void;
}
const {CalendarModule} = NativeModules;

export default CalendarModule as CalendarInterface;
