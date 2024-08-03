import React, { useEffect, useState } from 'react';
import { useLocation, useParams } from 'react-router-dom';


export const PermissionError = () => {
    const {state} = useLocation();

    return (
        <div>
            <h1>You were forbidden access due to:</h1>
            <h2>{state}</h2>
        </div>
    );
};

export default PermissionError;