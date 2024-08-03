import React, { useEffect, useState } from 'react';
import StoreModel from '../models/StoreModel';
import { getStoreInfo, getStoreManagers, getStoreOwners, isManager, isOwner, searchAndFilterStoreProducts, sendOwnerRequest,sendManagerRequest, hasPermission } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import ProductModel from '../models/ProductModel';
import { Rating } from 'react-simple-star-rating'
import '../styles/Staff.css';
import { IoPersonAdd } from "react-icons/io5";
import ProductInStore from './ProductInStore';
import ActionDropdown from './ActionDropdown';
import RestResponse from "../models/RestResponse";
import MemberModel from '../models/MemberModel';
import { TextField } from '@mui/material';
import StaffRow from './StaffRow';
import Permission from '../models/Permission';

export const Staff = () => {
    const {storeId} = useParams();
    const [managers, setManagers] = useState<MemberModel[]>([]);
    const [owners, setOwners] = useState<MemberModel[]>([]);
    const [appointManager, setAppointManager] = useState(false);
    const [appointOwner, setAppointOwner] = useState(false);
    const [managerUsername,setManagerUsername] = useState("");
    const [ownerUsername,setOwnerUsername] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        const checkAllowed = async ()=> {
            let canAccess: boolean = await hasPermission(storeId!, Permission.VIEW_ROLES);
            if(!canAccess){
                navigate('/permission-error', {state: "You do not have permission to view and edit roles in the given store"})
            }else{
                setManagers(await getStoreManagers(storeId!))
                setOwners(await getStoreOwners(storeId!))
            }
        }
        checkAllowed();
    }, [])

    return (
        <div className='walled'>
            <div className='managers'>
                <h1>Managers:</h1>
                {managers.map(manager => <StaffRow member={manager} isManager={true} storeId={storeId}/>)}
                <button onClick={() => setAppointManager(!appointManager)} className='addStaff'><IoPersonAdd /> Add Manager</button>
                <div className={`appointArea ${appointManager ? 'visible' : 'notvisible'}`}>
                    <p className='appointLabel'>Send Request To:</p>
                    <TextField size='small' id="outlined-basic" label="Username" variant="outlined" value={managerUsername} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {setManagerUsername(event.target.value);}}/>
                    <button onClick = {async () =>{
                        let resp: RestResponse = await sendManagerRequest(managerUsername, storeId!);
                        if(resp.error){
                            alert(`Failed to send request: ${resp.errorString}`)
                        }else{
                            alert(`Request send successfully!`)
                            setAppointManager(false);
                            setManagerUsername("");
                        }
                    }}className='sendButton'>Send</button>
                </div>
            </div>
            <div className='owners'>
                <h1>Owners:</h1>
                {owners.map(owner => <StaffRow member={owner} isManager={false} storeId={storeId}/>)}
                <button onClick={() => setAppointOwner(!appointOwner)} className='addStaff'><IoPersonAdd /> Add Owner</button>
                <div className={`appointArea ${appointOwner ? 'visible' : 'notvisible'}`}>
                    <p className='appointLabel'>Send Request To:</p>
                    <TextField size='small' id="outlined-basic" label="Username" variant="outlined" value={ownerUsername} onChange={(event: React.ChangeEvent<HTMLInputElement>) => {setOwnerUsername(event.target.value);}}/>
                    <button onClick = {async () =>{
                        let resp: RestResponse = await sendOwnerRequest(ownerUsername, storeId!);
                        if(resp.error){
                            alert(`Failed to send request: ${resp.errorString}`)
                        }else{
                            alert(`Request send successfully!`)
                            setAppointOwner(false);
                            setOwnerUsername("");
                        }
                    }}className='sendButton'>Send</button>
                </div>
            </div>
        </div>
    );
};

export default Staff;