import React from "react";
import { Oval } from "react-loader-spinner";
import '../styles/Loading.css';


export const Loading = (props:any) =>{
    return (
        <div className="loadingdiv">
            <Oval height={40} width={40} color='#0000FF' secondaryColor="#4444FF" strokeWidth={7}/>
            <p>Please wait, {props.reason}</p>
        </div>
    )
}