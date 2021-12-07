import React from 'react';
import {LinearProgress, Typography} from "@mui/material";

type SubTitleProps = {
  text: string
  loading: boolean
}

export default function SubTitle(props: SubTitleProps) {

  return (
    <div>
      {props.loading
        ?
        <LinearProgress />
        :
        <Typography variant={"subtitle1"}>
          {`${props.text}`}
        </Typography>
      }
    </div>
  )

}